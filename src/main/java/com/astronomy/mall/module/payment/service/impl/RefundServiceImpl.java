package com.astronomy.mall.module.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.payment.dto.ApplyRefundDTO;
import com.astronomy.mall.module.payment.entity.Payment;
import com.astronomy.mall.module.payment.entity.Refund;
import com.astronomy.mall.module.payment.mapper.PaymentMapper;
import com.astronomy.mall.module.payment.mapper.RefundMapper;
import com.astronomy.mall.module.payment.service.RefundService;
import com.astronomy.mall.module.payment.vo.RefundVO;
import com.astronomy.mall.module.user.service.BalanceService;  // 2.4.4 新增
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private NotificationHelper notificationHelper;

    // 2.4.4 新增：余额服务，退款回钱包用
    @Autowired
    private BalanceService balanceService;

    // =====================================================================
    // 申请退款
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundVO applyRefund(ApplyRefundDTO dto, Long userId) {
        // 1. 查询订单
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 验证订单状态（只有待发货/待收货/已完成可申请退款）
        if (order.getStatus() != 1 && order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "当前订单状态不支持退款");
        }

        // 4. 查询已支付的支付记录
        LambdaQueryWrapper<Payment> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(Payment::getOrderId, dto.getOrderId())
                .eq(Payment::getStatus, 1)
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1");
        Payment payment = paymentMapper.selectOne(paymentWrapper);
        if (payment == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID);
        }

        // 5. 验证退款金额不超过实付金额
        if (dto.getRefundAmount().compareTo(payment.getPaymentAmount()) > 0) {
            throw new BusinessException(ResultCode.REFUND_AMOUNT_EXCEED);
        }

        // 6. 检查是否已有进行中的退款申请
        LambdaQueryWrapper<Refund> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.eq(Refund::getOrderId, dto.getOrderId())
                .in(Refund::getStatus, 0, 1, 3);
        if (refundMapper.selectOne(refundWrapper) != null) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "该订单已有退款申请");
        }

        // 7. 创建退款记录
        Refund refund = new Refund();
        refund.setRefundNo(generateRefundNo());
        refund.setPaymentId(payment.getId());
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setUserId(userId);
        refund.setRefundAmount(dto.getRefundAmount());
        refund.setRefundReason(dto.getRefundReason());
        refund.setRefundType(dto.getRefundType());
        refund.setStatus(0); // 待审核

        refundMapper.insert(refund);
        log.info("创建退款申请成功: refundNo={}, orderId={}", refund.getRefundNo(), refund.getOrderId());

        return convertToVO(refund);
    }

    // =====================================================================
    // 审核退款（管理端调用）
    // 2.4.4 新增：通过时判断原支付方式，余额支付的退回钱包
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(Long refundId, boolean approved, String adminRemark) {
        // 1. 查询退款记录
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "退款记录不存在");
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "该退款申请已审核，不可重复操作");
        }

        refund.setAdminRemark(adminRemark);
        refund.setAuditTime(LocalDateTime.now());

        if (!approved) {
            // ── 审核拒绝 ──────────────────────────────────────────────
            refund.setStatus(2);
            refundMapper.updateById(refund);
            log.info("退款审核拒绝: refundNo={}", refund.getRefundNo());
            return;
        }

        // ── 审核通过：查询原支付记录，确定退款去向 ────────────────────
        Payment payment = paymentMapper.selectById(refund.getPaymentId());
        if (payment == null) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "原支付记录不存在");
        }

        // 2.4.4 核心逻辑：余额支付(paymentType=3) → 退回钱包
        //                  其他支付方式            → 原渠道退款（此处模拟）
        if (payment.getPaymentType() == 3) {
            // 退款金额加回用户钱包，写流水（type=3 回收入账复用，或可扩展 type=5 退款入账）
            // remark 中注明退款单号方便对账
            String remark = "退款入账，退款单号：" + refund.getRefundNo();
            balanceService.changeBalance(
                    refund.getUserId(),
                    refund.getRefundAmount(),    // 正数 = 收入
                    3,                           // type: 3-回收入账（可改为 5-退款入账，视枚举而定）
                    remark,
                    refund.getOrderId(),
                    "refund"
            );
            log.info("余额退款到钱包成功: userId={}, amount={}, refundNo={}",
                    refund.getUserId(), refund.getRefundAmount(), refund.getRefundNo());
        } else {
            // 支付宝/微信：模拟原渠道退款成功（对接真实渠道时在此调用退款 API）
            log.info("原渠道退款（模拟）: paymentType={}, refundNo={}",
                    payment.getPaymentType(), refund.getRefundNo());
        }

        // 更新退款状态：3=退款成功
        refund.setStatus(3);
        refund.setRefundTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        // 更新订单状态为退款完成（如有此状态字段，视实际枚举调整）
        Order order = orderMapper.selectById(refund.getOrderId());
        if (order != null) {
            order.setStatus(5); // 5=退款完成（如无此状态可去掉此段）
            orderMapper.updateById(order);
        }

        log.info("退款审核通过并处理完成: refundNo={}", refund.getRefundNo());
    }

    // =====================================================================
    // 查询退款详情
    // =====================================================================

    @Override
    public RefundVO getRefundDetail(Long refundId, Long userId) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "退款记录不存在");
        }
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return convertToVO(refund);
    }

    @Override
    public RefundVO getRefundByOrderId(Long orderId, Long userId) {
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getOrderId, orderId)
                .eq(Refund::getUserId, userId)
                .orderByDesc(Refund::getCreateTime)
                .last("LIMIT 1");

        Refund refund = refundMapper.selectOne(wrapper);
        if (refund == null) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "退款记录不存在");
        }
        return convertToVO(refund);
    }

    @Override
    public Page<RefundVO> getRefundList(Long userId, Integer pageNum, Integer pageSize) {
        Page<Refund> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Refund::getUserId, userId)
                .orderByDesc(Refund::getCreateTime);

        Page<Refund> refundPage = refundMapper.selectPage(page, wrapper);

        Page<RefundVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(refundPage.getTotal());
        voPage.setRecords(refundPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    // =====================================================================
    // 取消退款申请（用户主动取消）
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRefund(Long refundId, Long userId) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "退款记录不存在");
        }
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "当前退款状态不允许取消");
        }

        refund.setStatus(2);
        refund.setAdminRemark("用户主动取消");
        refund.setAuditTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        log.info("取消退款申请成功: refundNo={}", refund.getRefundNo());
    }

    // =====================================================================
    // 私有工具方法
    // =====================================================================

    private String generateRefundNo() {
        String date   = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String random = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        return "REFUND" + date + random;
    }

    private RefundVO convertToVO(Refund refund) {
        return BeanUtil.copyProperties(refund, RefundVO.class);
    }
}
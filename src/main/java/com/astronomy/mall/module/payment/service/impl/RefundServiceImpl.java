package com.astronomy.mall.module.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.payment.dto.ApplyRefundDTO;
import com.astronomy.mall.module.payment.entity.Payment;
import com.astronomy.mall.module.payment.entity.Refund;
import com.astronomy.mall.module.payment.mapper.PaymentMapper;
import com.astronomy.mall.module.payment.mapper.RefundMapper;
import com.astronomy.mall.module.payment.service.RefundService;
import com.astronomy.mall.module.payment.vo.RefundVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 3. 验证订单状态(只有待发货、待收货、已完成的订单才能申请退款)
        if (order.getStatus() != 1 && order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "当前订单状态不支持退款");
        }

        // 4. 查询支付记录
        LambdaQueryWrapper<Payment> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(Payment::getOrderId, dto.getOrderId())
                .eq(Payment::getStatus, 1) // 已支付
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1");
        Payment payment = paymentMapper.selectOne(paymentWrapper);

        if (payment == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID);
        }

        // 5. 验证退款金额
        if (dto.getRefundAmount().compareTo(payment.getPaymentAmount()) > 0) {
            throw new BusinessException(ResultCode.REFUND_AMOUNT_EXCEED);
        }

        // 6. 检查是否已有退款申请
        LambdaQueryWrapper<Refund> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.eq(Refund::getOrderId, dto.getOrderId())
                .in(Refund::getStatus, 0, 1, 3); // 待审核、审核通过、退款成功
        Refund existRefund = refundMapper.selectOne(refundWrapper);
        if (existRefund != null) {
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

        // 转换VO
        Page<RefundVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(refundPage.getTotal());
        List<RefundVO> voList = refundPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRefund(Long refundId, Long userId) {
        // 1. 查询退款记录
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "退款记录不存在");
        }

        // 2. 验证归属
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 只有待审核状态才能取消
        if (refund.getStatus() != 0) {
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "当前退款状态不允许取消");
        }

        // 4. 更新状态为审核拒绝
        refund.setStatus(2);
        refund.setAdminRemark("用户主动取消");
        refund.setAuditTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        log.info("取消退款申请成功: refundNo={}", refund.getRefundNo());
    }

    /**
     * 生成退款单号
     * 格式: REFUND + 年月日 + 6位随机数
     */
    private String generateRefundNo() {
        String date = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String random = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        return "REFUND" + date + random;
    }

    /**
     * 转换为VO
     */
    private RefundVO convertToVO(Refund refund) {
        RefundVO vo = BeanUtil.copyProperties(refund, RefundVO.class);
        return vo;
    }
}
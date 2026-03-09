package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.RefundAuditDTO;
import com.astronomy.mall.module.admin.dto.RefundQueryDTO;
import com.astronomy.mall.module.admin.service.AdminRefundService;
import com.astronomy.mall.module.admin.vo.AdminRefundDetailVO;
import com.astronomy.mall.module.admin.vo.AdminRefundVO;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.entity.OrderItem;
import com.astronomy.mall.module.order.mapper.OrderItemMapper;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.payment.entity.Payment;
import com.astronomy.mall.module.payment.entity.Refund;
import com.astronomy.mall.module.payment.mapper.PaymentMapper;
import com.astronomy.mall.module.payment.mapper.RefundMapper;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.astronomy.mall.module.user.service.BalanceService;   // 2.4.4 新增
import com.astronomy.mall.utils.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台退款管理 ServiceImpl
 *
 * 📌 2.4.4 改造说明：
 *   doProcessRefund() 新增支付方式判断：
 *   - paymentType = 3（余额支付）→ 退款金额加回用户钱包，写余额流水
 *   - paymentType = 1/2（支付宝/微信）→ 原逻辑不变，模拟原渠道退款
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRefundServiceImpl implements AdminRefundService {

    private final RefundMapper       refundMapper;
    private final PaymentMapper      paymentMapper;
    private final OrderMapper        orderMapper;
    private final OrderItemMapper    orderItemMapper;
    private final UserMapper         userMapper;
    private final NotificationHelper notificationHelper;
    private final BalanceService     balanceService;   // 2.4.4 新增

    // =============================================
    // 退款列表（分页）
    // =============================================
    @Override
    public Page<AdminRefundVO> getRefundList(RefundQueryDTO queryDTO) {
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getStatus() != null)                 wrapper.eq(Refund::getStatus,     queryDTO.getStatus());
        if (StringUtils.hasText(queryDTO.getOrderNo()))   wrapper.like(Refund::getOrderNo,  queryDTO.getOrderNo());
        if (StringUtils.hasText(queryDTO.getRefundNo()))  wrapper.like(Refund::getRefundNo, queryDTO.getRefundNo());
        if (queryDTO.getUserId() != null)                 wrapper.eq(Refund::getUserId,     queryDTO.getUserId());
        if (StringUtils.hasText(queryDTO.getStartTime())) wrapper.ge(Refund::getCreateTime, queryDTO.getStartTime());
        if (StringUtils.hasText(queryDTO.getEndTime()))   wrapper.le(Refund::getCreateTime, queryDTO.getEndTime() + " 23:59:59");
        wrapper.orderByDesc(Refund::getCreateTime);

        Page<Refund> refundPage = refundMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        Page<AdminRefundVO> voPage = new Page<>(refundPage.getCurrent(), refundPage.getSize(), refundPage.getTotal());
        voPage.setRecords(refundPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    // =============================================
    // 退款详情
    // =============================================
    @Override
    public AdminRefundDetailVO getRefundDetail(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) throw new BusinessException("退款记录不存在");

        AdminRefundDetailVO detailVO = new AdminRefundDetailVO();
        BeanUtils.copyProperties(refund, detailVO);
        detailVO.setRefundTypeDesc(getRefundTypeDesc(refund.getRefundType()));
        detailVO.setStatusDesc(getStatusDesc(refund.getStatus()));

        User user = userMapper.selectById(refund.getUserId());
        if (user != null) {
            detailVO.setUsername(user.getUsername());
            detailVO.setNickname(user.getNickname());
            detailVO.setPhone(user.getPhone());
        }

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order != null) {
            detailVO.setOrderAmount(order.getTotalAmount());
            detailVO.setReceiverName(order.getReceiverName());
            detailVO.setReceiverPhone(order.getReceiverPhone());
            detailVO.setReceiverAddress(order.getReceiverAddress());
            detailVO.setOrderStatus(order.getStatus());
            detailVO.setOrderCreateTime(order.getCreateTime());
        }

        Payment payment = paymentMapper.selectById(refund.getPaymentId());
        if (payment != null) {
            detailVO.setPaymentNo(payment.getPaymentNo());
            detailVO.setPaymentType(payment.getPaymentType());
            detailVO.setPaymentTypeDesc(getPaymentTypeDesc(payment.getPaymentType()));
            detailVO.setPaymentAmount(payment.getPaymentAmount());
            detailVO.setPaymentTime(payment.getPaymentTime());
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, refund.getOrderId()));
        detailVO.setOrderItems(orderItems.stream().map(item -> {
            AdminRefundDetailVO.OrderItemVO itemVO = new AdminRefundDetailVO.OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList()));

        return detailVO;
    }

    // =============================================
    // 审核通过 → 自动触发 doProcessRefund
    // =============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long id, RefundAuditDTO auditDTO) {
        Refund refund = getAndCheckRefund(id, 0, "只能审核待审核状态的退款");

        Long adminId = UserContext.getUserId();
        User admin = userMapper.selectById(adminId);

        refund.setStatus(1);
        refund.setAdminId(adminId);
        refund.setAdminRemark(auditDTO.getAdminRemark());
        refund.setAuditTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        log.info("退款审核通过: refundId={}, adminId={}", id, adminId);

        notificationHelper.sendRefundApprovedNotification(
                refund.getUserId(),
                refund.getRefundAmount().toPlainString(),
                refund.getId(),
                refund.getOrderId()
        );

        // 审核通过后立即执行退款（内部判断支付方式决定退款去向）
        doProcessRefund(refund);
    }

    // =============================================
    // 审核拒绝
    // =============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(Long id, RefundAuditDTO auditDTO) {
        Refund refund = getAndCheckRefund(id, 0, "只能审核待审核状态的退款");

        Long adminId = UserContext.getUserId();
        refund.setStatus(2);
        refund.setAdminId(adminId);
        refund.setAdminRemark(auditDTO.getAdminRemark());
        refund.setAuditTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        log.info("退款审核拒绝: refundId={}, adminId={}, reason={}", id, adminId, auditDTO.getAdminRemark());

        String reason = StringUtils.hasText(auditDTO.getAdminRemark())
                ? auditDTO.getAdminRemark() : "不符合退款条件";
        notificationHelper.sendRefundRejectedNotification(
                refund.getUserId(), reason, refund.getId(), refund.getOrderId());
    }

    // =============================================
    // 手动处理退款（失败重试）
    // =============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) throw new BusinessException("退款记录不存在");
        if (refund.getStatus() != 1 && refund.getStatus() != 4) {
            throw new BusinessException("只能处理审核通过或退款失败的退款");
        }
        doProcessRefund(refund);
    }

    // =============================================
    // 私有：执行退款处理
    // 📌 2.4.4 改造核心：余额支付退回钱包
    // =============================================
    private void doProcessRefund(Refund refund) {
        try {
            log.info("开始处理退款: refundId={}, amount={}", refund.getId(), refund.getRefundAmount());

            // 查询原支付记录，确定支付方式
            Payment payment = paymentMapper.selectById(refund.getPaymentId());
            if (payment == null) {
                throw new BusinessException("原支付记录不存在");
            }

            if (payment.getPaymentType() == 3) {
                // ── 余额支付 → 退款金额加回钱包，写流水 ──────────────
                String remark = "退款入账，退款单号：" + refund.getRefundNo();
                balanceService.changeBalance(
                        refund.getUserId(),
                        refund.getRefundAmount(),   // 正数 = 收入
                        3,                          // type: 3=回收入账
                        remark,
                        refund.getOrderId(),
                        "refund"
                );
                log.info("余额退款到钱包成功: userId={}, amount={}, refundNo={}",
                        refund.getUserId(), refund.getRefundAmount(), refund.getRefundNo());
            } else {
                // ── 支付宝/微信 → 模拟原渠道退款 ──────────────────────
                log.info("原渠道退款（模拟）: paymentType={}, refundNo={}",
                        payment.getPaymentType(), refund.getRefundNo());
            }

            // 退款状态 → 退款成功(3)
            refund.setStatus(3);
            refund.setRefundTime(LocalDateTime.now());
            refundMapper.updateById(refund);

            // 支付记录 → 已退款(3)
            payment.setStatus(3);
            paymentMapper.updateById(payment);

            // 订单状态：仅待发货(1) → 已取消(4)，其他状态不动
            Order order = orderMapper.selectById(refund.getOrderId());
            if (order != null && order.getStatus() == 1) {
                order.setStatus(4);
                orderMapper.updateById(order);
                log.info("订单状态同步为已取消: orderId={}", order.getId());
            }

            log.info("退款处理成功: refundId={}", refund.getId());

            notificationHelper.sendRefundCompletedNotification(
                    refund.getUserId(),
                    refund.getRefundAmount().toPlainString(),
                    refund.getId(),
                    refund.getOrderId()
            );

        } catch (Exception e) {
            log.error("退款处理失败: refundId={}", refund.getId(), e);
            refund.setStatus(4); // 退款失败
            refundMapper.updateById(refund);
            throw new BusinessException("退款处理失败，请稍后重试: " + e.getMessage());
        }
    }

    // =============================================
    // 私有：查询并校验退款状态
    // =============================================
    private Refund getAndCheckRefund(Long id, Integer expectedStatus, String errorMsg) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) throw new BusinessException("退款记录不存在");
        if (!refund.getStatus().equals(expectedStatus)) throw new BusinessException(errorMsg);
        return refund;
    }

    // =============================================
    // 私有：Refund → AdminRefundVO
    // =============================================
    private AdminRefundVO convertToVO(Refund refund) {
        AdminRefundVO vo = new AdminRefundVO();
        BeanUtils.copyProperties(refund, vo);
        vo.setRefundTypeDesc(getRefundTypeDesc(refund.getRefundType()));
        vo.setStatusDesc(getStatusDesc(refund.getStatus()));
        User user = userMapper.selectById(refund.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        return vo;
    }

    // =============================================
    // 枚举描述
    // =============================================
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "审核通过";
            case 2: return "审核拒绝";
            case 3: return "退款成功";
            case 4: return "退款失败";
            default: return "未知";
        }
    }

    private String getRefundTypeDesc(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "仅退款";
            case 2: return "退货退款";
            default: return "未知";
        }
    }

    private String getPaymentTypeDesc(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "支付宝";
            case 2: return "微信支付";
            case 3: return "余额支付";
            default: return "未知";
        }
    }
}
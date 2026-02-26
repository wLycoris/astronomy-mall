package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.Result;
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
 * 后台退款管理ServiceImpl
 *
 * 文件路径: com.astronomy.mall.module.admin.service.impl.AdminRefundServiceImpl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRefundServiceImpl implements AdminRefundService {

    private final RefundMapper refundMapper;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final NotificationHelper notificationHelper;

    // =============================================
    // 退款列表（分页）—— 返回 Page<AdminRefundVO>
    // =============================================
    @Override
    public Page<AdminRefundVO> getRefundList(RefundQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getStatus() != null) {
            wrapper.eq(Refund::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.like(Refund::getOrderNo, queryDTO.getOrderNo());
        }
        if (StringUtils.hasText(queryDTO.getRefundNo())) {
            wrapper.like(Refund::getRefundNo, queryDTO.getRefundNo());
        }
        if (queryDTO.getUserId() != null) {
            wrapper.eq(Refund::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            wrapper.ge(Refund::getCreateTime, queryDTO.getStartTime());
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            wrapper.le(Refund::getCreateTime, queryDTO.getEndTime() + " 23:59:59");
        }
        wrapper.orderByDesc(Refund::getCreateTime);

        // 分页查询
        Page<Refund> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Refund> refundPage = refundMapper.selectPage(page, wrapper);

        // 转换 VO，手动构建 Page<AdminRefundVO>
        Page<AdminRefundVO> voPage = new Page<>(refundPage.getCurrent(), refundPage.getSize(), refundPage.getTotal());
        List<AdminRefundVO> voList = refundPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    // =============================================
    // 退款详情
    // =============================================
    @Override
    public AdminRefundDetailVO getRefundDetail(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }

        AdminRefundDetailVO detailVO = new AdminRefundDetailVO();
        BeanUtils.copyProperties(refund, detailVO);
        detailVO.setRefundTypeDesc(getRefundTypeDesc(refund.getRefundType()));
        detailVO.setStatusDesc(getStatusDesc(refund.getStatus()));

        // 用户信息
        User user = userMapper.selectById(refund.getUserId());
        if (user != null) {
            detailVO.setUsername(user.getUsername());
            detailVO.setNickname(user.getNickname());
            detailVO.setPhone(user.getPhone());
        }

        // 订单信息
        Order order = orderMapper.selectById(refund.getOrderId());
        if (order != null) {
            detailVO.setOrderAmount(order.getTotalAmount());
            detailVO.setReceiverName(order.getReceiverName());
            detailVO.setReceiverPhone(order.getReceiverPhone());
            detailVO.setReceiverAddress(order.getReceiverAddress());
            detailVO.setOrderStatus(order.getStatus());
            detailVO.setOrderCreateTime(order.getCreateTime());
        }

        // 支付信息
        Payment payment = paymentMapper.selectById(refund.getPaymentId());
        if (payment != null) {
            detailVO.setPaymentNo(payment.getPaymentNo());
            detailVO.setPaymentType(payment.getPaymentType());
            detailVO.setPaymentTypeDesc(getPaymentTypeDesc(payment.getPaymentType()));
            detailVO.setPaymentAmount(payment.getPaymentAmount());
            detailVO.setPaymentTime(payment.getPaymentTime());
        }

        // 订单商品列表
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, refund.getOrderId())
        );
        List<AdminRefundDetailVO.OrderItemVO> itemVOList = orderItems.stream().map(item -> {
            AdminRefundDetailVO.OrderItemVO itemVO = new AdminRefundDetailVO.OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList());
        detailVO.setOrderItems(itemVOList);

        return detailVO;
    }

    // =============================================
    // 审核通过
    // =============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long id, RefundAuditDTO auditDTO) {
        Refund refund = getAndCheckRefund(id, 0, "只能审核待审核状态的退款");

        // 从 ThreadLocal 获取当前管理员信息（JwtInterceptor 存入）
        Long adminId = UserContext.getUserId();
        User admin = userMapper.selectById(adminId);
        String adminName = admin != null ? admin.getUsername() : "管理员";

        // 更新退款状态为审核通过
        refund.setStatus(1);
        refund.setAdminId(adminId);
        refund.setAdminRemark(auditDTO.getAdminRemark());
        refund.setAuditTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        log.info("退款审核通过: refundId={}, adminId={}", id, adminId);

        // 发送审核通过通知（NotificationHelper内部已@Async）
        notificationHelper.sendRefundApprovedNotification(
                refund.getUserId(),
                refund.getRefundAmount().toPlainString(),
                refund.getId(),
                refund.getOrderId()    // ← 新增
        );

        // 审核通过后自动处理退款
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

        // 更新退款状态为审核拒绝
        refund.setStatus(2);
        refund.setAdminId(adminId);
        refund.setAdminRemark(auditDTO.getAdminRemark());
        refund.setAuditTime(LocalDateTime.now());
        refundMapper.updateById(refund);

        log.info("退款审核拒绝: refundId={}, adminId={}, reason={}", id, adminId, auditDTO.getAdminRemark());

        // 发送审核拒绝通知
        String reason = StringUtils.hasText(auditDTO.getAdminRemark())
                ? auditDTO.getAdminRemark() : "不符合退款条件";
        notificationHelper.sendRefundRejectedNotification(
                refund.getUserId(),
                reason,
                refund.getId(),
                refund.getOrderId()    // ← 新增
        );
    }

    // =============================================
    // 手动处理退款（对外接口，用于失败重试）
    // =============================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        // 只允许处理审核通过(1)或退款失败(4)的记录
        if (refund.getStatus() != 1 && refund.getStatus() != 4) {
            throw new BusinessException("只能处理审核通过或退款失败的退款");
        }
        doProcessRefund(refund);
    }

    // =============================================
    // 私有方法：执行退款处理（模拟退款）
    // =============================================
    private void doProcessRefund(Refund refund) {
        try {
            log.info("开始处理退款: refundId={}, amount={}", refund.getId(), refund.getRefundAmount());

            // 1. 更新退款状态为退款成功(3)
            refund.setStatus(3);
            refund.setRefundTime(LocalDateTime.now());
            refundMapper.updateById(refund);

            // 2. 更新支付记录状态为已退款(3)
            Payment payment = paymentMapper.selectById(refund.getPaymentId());
            if (payment != null) {
                payment.setStatus(3);
                paymentMapper.updateById(payment);
            }

            // 3. 同步更新订单状态
            //    待发货(1)的订单退款 → 改为已取消(4)
            //    待收货(2)/已完成(3)的订单退款 → 保持订单状态不变（钱已退，商品已发/已收）
            Order order = orderMapper.selectById(refund.getOrderId());
            if (order != null && order.getStatus() == 1) {
                order.setStatus(4); // 已取消
                orderMapper.updateById(order);
                log.info("订单状态同步为已取消: orderId={}", order.getId());
            }

            log.info("退款处理成功: refundId={}", refund.getId());

            // 4. 发送退款到账通知
            notificationHelper.sendRefundCompletedNotification(
                    refund.getUserId(),
                    refund.getRefundAmount().toPlainString(),
                    refund.getId(),
                    refund.getOrderId()    // ← 新增
            );
        } catch (Exception e) {
            log.error("退款处理失败: refundId={}", refund.getId(), e);
            refund.setStatus(4);
            refundMapper.updateById(refund);
            throw new BusinessException("退款处理失败，请稍后重试: " + e.getMessage());
        }
    }

    // =============================================
    // 私有方法：查询并校验退款状态
    // =============================================
    private Refund getAndCheckRefund(Long id, Integer expectedStatus, String errorMsg) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (!refund.getStatus().equals(expectedStatus)) {
            throw new BusinessException(errorMsg);
        }
        return refund;
    }

    // =============================================
    // 私有方法：Refund → AdminRefundVO
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
package com.astronomy.mall.module.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.payment.dto.*;
import com.astronomy.mall.module.payment.entity.Payment;
import com.astronomy.mall.module.payment.mapper.PaymentMapper;
import com.astronomy.mall.module.payment.service.PaymentService;
import com.astronomy.mall.module.payment.vo.PaymentVO;
import com.astronomy.mall.module.user.service.BalanceService;   // 2.4.4 新增
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
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private NotificationHelper notificationHelper;

    // 📌 2.4.4 新增：余额服务（payment_type=3 真实扣款）
    @Autowired
    private BalanceService balanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(CreatePaymentDTO dto, Long userId) {
        // 1. 查询订单
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 验证订单状态(只有待支付的订单才能支付)
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 4. 验证支付金额
        if (dto.getPaymentAmount().compareTo(order.getPaymentAmount()) != 0) {
            throw new BusinessException(ResultCode.PAYMENT_AMOUNT_ERROR);
        }

        // 5. 检查是否已存在支付记录
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, dto.getOrderId())
                .in(Payment::getStatus, 0, 1);
        Payment existPayment = paymentMapper.selectOne(wrapper);
        if (existPayment != null) {
            if (existPayment.getStatus() == 1) {
                throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
            }
            return convertToVO(existPayment);
        }

        // 6. 创建支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setPaymentType(dto.getPaymentType());
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setStatus(0); // 待支付

        paymentMapper.insert(payment);
        log.info("创建支付订单成功: paymentNo={}, orderId={}", payment.getPaymentNo(), payment.getOrderId());

        return convertToVO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatePaymentSuccess(Long paymentId, Long userId) {
        // 1. 查询支付记录
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "支付记录不存在");
        }

        // 2. 验证归属
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 验证支付状态
        if (payment.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }

        // ── 2.4.4 改造：payment_type=3（余额支付）真实扣款 ──────────────
        // 之前是空壳，现在调用 BalanceService 真实扣减余额
        // BalanceService 内部使用 SELECT...FOR UPDATE 行锁防并发
        if (payment.getPaymentType() == 3) {
            Order orderForBalance = orderMapper.selectById(payment.getOrderId());
            balanceService.changeBalance(
                    userId,
                    payment.getPaymentAmount().negate(),   // 负数=支出
                    4,                                     // type=4 购物扣款
                    "订单支付: " + (orderForBalance != null ? orderForBalance.getOrderNo() : payment.getOrderNo()),
                    payment.getOrderId(),
                    "order"
            );
            log.info("余额支付成功: paymentNo={}, amount={}", payment.getPaymentNo(), payment.getPaymentAmount());
        }
        // ── 余额支付改造结束 ─────────────────────────────────────────────

        // 4. 更新支付状态
        payment.setStatus(1);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTransactionId("MOCK_" + System.currentTimeMillis());
        paymentMapper.updateById(payment);

        // 5. 更新订单状态
        Order order = orderMapper.selectById(payment.getOrderId());
        if (order != null) {
            order.setStatus(1); // 待发货
            order.setPaymentTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        log.info("模拟支付成功: paymentNo={}, orderId={}", payment.getPaymentNo(), payment.getOrderId());

        // 6. 发送支付成功通知
        if (order != null) {
            notificationHelper.sendOrderPaidNotification(
                    order.getUserId(),
                    order.getOrderNo(),
                    order.getPaymentAmount().toString(),
                    order.getId()
            );
        }
    }

    @Override
    public PaymentVO getPaymentStatus(Long paymentId, Long userId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "支付记录不存在");
        }

        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return convertToVO(payment);
    }

    @Override
    public PaymentVO getPaymentByOrderId(Long orderId, Long userId) {
        // 🔧 v8.58 修复：查无记录时返回 null 而不是抛异常。
        // 理由：PaymentPage 初始化会调用本接口检查是否有已存在的支付记录，
        //      首次进入时理应查不到 — 这是正常业务状态，不应触发全局 error toast。
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId)
                .eq(Payment::getUserId, userId)
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1");

        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            return null;
        }
        return convertToVO(payment);
    }

    @Override
    public Page<PaymentVO> getPaymentList(Long userId, Integer pageNum, Integer pageSize) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getUserId, userId)
                .orderByDesc(Payment::getCreateTime);

        Page<Payment> paymentPage = paymentMapper.selectPage(page, wrapper);

        Page<PaymentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(paymentPage.getTotal());
        List<PaymentVO> voList = paymentPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    private String generatePaymentNo() {
        String date = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String random = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        return "PAY" + date + random;
    }

    private PaymentVO convertToVO(Payment payment) {
        return BeanUtil.copyProperties(payment, PaymentVO.class);
    }
}
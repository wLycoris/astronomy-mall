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

    // 🔥 新增：注入通知助手
    @Autowired
    private NotificationHelper notificationHelper;

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
                .in(Payment::getStatus, 0, 1); // 待支付或已支付
        Payment existPayment = paymentMapper.selectOne(wrapper);
        if (existPayment != null) {
            if (existPayment.getStatus() == 1) {
                throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
            }
            // 如果是待支付状态,返回已有的支付记录
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

        // 4. 更新支付状态
        payment.setStatus(1); // 支付成功
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

        // 🔥 6. 发送支付成功通知
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
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId)
                .eq(Payment::getUserId, userId)
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1");

        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "支付记录不存在");
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

        // 转换VO
        Page<PaymentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(paymentPage.getTotal());
        List<PaymentVO> voList = paymentPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 生成支付流水号
     * 格式: PAY + 年月日 + 6位随机数
     */
    private String generatePaymentNo() {
        String date = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String random = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        return "PAY" + date + random;
    }

    /**
     * 转换为VO
     */
    private PaymentVO convertToVO(Payment payment) {
        PaymentVO vo = BeanUtil.copyProperties(payment, PaymentVO.class);
        return vo;
    }
}
package com.astronomy.mall.module.payment.service;

import com.astronomy.mall.module.payment.dto.*;
import com.astronomy.mall.module.payment.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付订单
     */
    PaymentVO createPayment(CreatePaymentDTO dto, Long userId);

    /**
     * 模拟支付成功
     */
    void simulatePaymentSuccess(Long paymentId, Long userId);

    /**
     * 查询支付状态
     */
    PaymentVO getPaymentStatus(Long paymentId, Long userId);

    /**
     * 根据订单ID查询支付记录
     */
    PaymentVO getPaymentByOrderId(Long orderId, Long userId);

    /**
     * 分页查询支付记录
     */
    Page<PaymentVO> getPaymentList(Long userId, Integer pageNum, Integer pageSize);
}
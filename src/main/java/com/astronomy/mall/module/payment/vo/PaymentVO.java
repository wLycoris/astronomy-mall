package com.astronomy.mall.module.payment.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录VO
 */
@Data
public class PaymentVO {

    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private Integer paymentType;
    private String paymentTypeName; // 支付方式名称
    private BigDecimal paymentAmount;
    private Integer status;
    private String statusName; // 状态名称
    private LocalDateTime paymentTime;
    private String transactionId;
    private LocalDateTime createTime;

    public String getPaymentTypeName() {
        if (paymentType == null) return "未知";
        switch (paymentType) {
            case 1: return "支付宝";
            case 2: return "微信支付";
            case 3: return "余额支付";
            default: return "未知";
        }
    }

    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "支付成功";
            case 2: return "支付失败";
            case 3: return "已退款";
            default: return "未知";
        }
    }
}
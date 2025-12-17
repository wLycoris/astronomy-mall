package com.astronomy.mall.module.payment.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 创建支付DTO
 */
@Data
public class CreatePaymentDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式无效")
    @Max(value = 3, message = "支付方式无效")
    private Integer paymentType; // 1-支付宝 2-微信 3-余额

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal paymentAmount;
}
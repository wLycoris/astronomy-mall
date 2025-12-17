package com.astronomy.mall.module.payment.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 申请退款DTO
 */
@Data
public class ApplyRefundDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    @NotBlank(message = "退款原因不能为空")
    @Size(max = 200, message = "退款原因不能超过200字")
    private String refundReason;

    private Integer refundType = 1; // 1-仅退款 2-退货退款
}
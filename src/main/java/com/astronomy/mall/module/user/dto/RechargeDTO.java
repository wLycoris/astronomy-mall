package com.astronomy.mall.module.user.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 模拟充值请求 DTO
 *
 * 文件路径: com.astronomy.mall.module.user.dto.RechargeDTO
 */
@Data
public class RechargeDTO {

    /**
     * 充值金额
     * 范围: 0.01 ~ 99999.99
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额最小0.01元")
    @DecimalMax(value = "99999.99", message = "充值金额最大99999.99元")
    private BigDecimal amount;
}
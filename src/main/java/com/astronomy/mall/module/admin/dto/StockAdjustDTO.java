package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.*;

/**
 * 库存调整DTO
 */
@Data
public class StockAdjustDTO {

    /**
     * 调整类型(1-增加 2-减少)
     */
    @NotNull(message = "调整类型不能为空")
    @Min(value = 1, message = "调整类型只能是1或2")
    @Max(value = 2, message = "调整类型只能是1或2")
    private Integer adjustType;

    /**
     * 调整数量
     */
    @NotNull(message = "调整数量不能为空")
    @Min(value = 1, message = "调整数量必须大于0")
    private Integer quantity;

    /**
     * 调整原因
     */
    @NotBlank(message = "调整原因不能为空")
    @Size(max = 200, message = "调整原因不能超过200个字符")
    private String reason;
}
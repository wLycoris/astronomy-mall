package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 订单取消DTO
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Data
@ApiModel("订单取消DTO")
public class OrderCancelDTO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Long orderId;

    @NotBlank(message = "取消原因不能为空")
    @ApiModelProperty(value = "取消原因", required = true, example = "客户要求取消")
    private String cancelReason;
}
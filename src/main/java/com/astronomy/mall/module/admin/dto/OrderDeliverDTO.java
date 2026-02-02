package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 订单派送DTO
 *
 * @author astronomy-mall
 * @date 2026-01-29
 */
@Data
@ApiModel("订单派送DTO")
public class OrderDeliverDTO {

    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
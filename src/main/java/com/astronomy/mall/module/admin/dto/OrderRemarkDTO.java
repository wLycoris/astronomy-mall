package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 订单备注DTO
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Data
@ApiModel("订单备注DTO")
public class OrderRemarkDTO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Long orderId;

    @NotBlank(message = "备注内容不能为空")
    @ApiModelProperty(value = "备注内容", required = true, example = "客户要求加急配送")
    private String remark;
}
package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 订单发货DTO
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Data
@ApiModel("订单发货DTO")
public class OrderShipDTO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Long orderId;

    @NotBlank(message = "物流公司不能为空")
    @ApiModelProperty(value = "物流公司", required = true, example = "顺丰速运")
    private String logisticsCompany;

    @NotBlank(message = "物流单号不能为空")
    @ApiModelProperty(value = "物流单号", required = true, example = "SF1234567890")
    private String trackingNumber;

    @ApiModelProperty(value = "备注", example = "已发货,请注意查收")
    private String remark;
}
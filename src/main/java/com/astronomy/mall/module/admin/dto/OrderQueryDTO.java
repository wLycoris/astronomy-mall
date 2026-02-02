package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单查询DTO
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Data
@ApiModel("订单查询DTO")
public class OrderQueryDTO {

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String username;

    @ApiModelProperty("收货人姓名")
    private String receiverName;

    @ApiModelProperty("收货人电话")
    private String receiverPhone;

    @ApiModelProperty("订单状态(0-待支付 1-待发货 2-待收货 3-已完成 4-已取消)")
    private Integer status;

    @ApiModelProperty("物流状态(0-未发货 1-运输中 2-派送中 3-已签收)")
    private Integer logisticsStatus;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("当前页码")
    private Integer current = 1;

    @ApiModelProperty("每页条数")
    private Integer size = 10;
}
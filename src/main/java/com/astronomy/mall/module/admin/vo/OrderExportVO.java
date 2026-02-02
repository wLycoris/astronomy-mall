package com.astronomy.mall.module.admin.vo;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单导出VO
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Data
@ApiModel("订单导出VO")
public class OrderExportVO {

    @Alias("订单编号")
    @ApiModelProperty("订单编号")
    private String orderNo;

    @Alias("用户名")
    @ApiModelProperty("用户名")
    private String username;

    @Alias("收货人")
    @ApiModelProperty("收货人")
    private String receiverName;

    @Alias("联系电话")
    @ApiModelProperty("联系电话")
    private String receiverPhone;

    @Alias("收货地址")
    @ApiModelProperty("收货地址")
    private String fullAddress;

    @Alias("商品总金额")
    @ApiModelProperty("商品总金额")
    private BigDecimal totalAmount;

    @Alias("运费")
    @ApiModelProperty("运费")
    private BigDecimal freight;

    @Alias("优惠金额")
    @ApiModelProperty("优惠金额")
    private BigDecimal discountAmount;

    @Alias("实付金额")
    @ApiModelProperty("实付金额")
    private BigDecimal paymentAmount;

    @Alias("订单状态")
    @ApiModelProperty("订单状态")
    private String statusName;

    @Alias("物流公司")
    @ApiModelProperty("物流公司")
    private String logisticsCompany;

    @Alias("物流单号")
    @ApiModelProperty("物流单号")
    private String trackingNumber;

    @Alias("物流状态")
    @ApiModelProperty("物流状态")
    private String logisticsStatusName;

    @Alias("管理员备注")
    @ApiModelProperty("管理员备注")
    private String adminRemark;

    @Alias("创建时间")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Alias("支付时间")
    @ApiModelProperty("支付时间")
    private String paymentTime;

    @Alias("发货时间")
    @ApiModelProperty("发货时间")
    private String deliveryTime;
}
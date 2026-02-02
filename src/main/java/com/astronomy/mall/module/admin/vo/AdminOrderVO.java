package com.astronomy.mall.module.admin.vo;

import com.astronomy.mall.module.order.vo.OrderItemVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员订单VO
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Data
@ApiModel("管理员订单VO")
public class AdminOrderVO {

    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("收货人姓名")
    private String receiverName;

    @ApiModelProperty("收货人电话")
    private String receiverPhone;

    @ApiModelProperty("收货省份")
    private String receiverProvince;

    @ApiModelProperty("收货城市")
    private String receiverCity;

    @ApiModelProperty("收货区县")
    private String receiverDistrict;

    @ApiModelProperty("详细地址")
    private String receiverAddress;

    @ApiModelProperty("完整地址")
    private String fullAddress;

    @ApiModelProperty("商品总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("运费")
    private BigDecimal freight;

    @ApiModelProperty("优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty("实付金额")
    private BigDecimal paymentAmount;

    @ApiModelProperty("订单状态(0-待支付 1-待发货 2-待收货 3-已完成 4-已取消)")
    private Integer status;

    @ApiModelProperty("订单状态名称")
    private String statusName;

    @ApiModelProperty("支付时间")
    private LocalDateTime paymentTime;

    @ApiModelProperty("发货时间")
    private LocalDateTime deliveryTime;

    @ApiModelProperty("完成时间")
    private LocalDateTime finishTime;

    @ApiModelProperty("取消时间")
    private LocalDateTime cancelTime;

    @ApiModelProperty("订单备注")
    private String remark;

    @ApiModelProperty("取消原因")
    private String cancelReason;

    @ApiModelProperty("物流公司")
    private String logisticsCompany;

    @ApiModelProperty("物流单号")
    private String trackingNumber;

    @ApiModelProperty("物流状态(0-未发货 1-运输中 2-派送中 3-已签收)")
    private Integer logisticsStatus;

    @ApiModelProperty("物流状态名称")
    private String logisticsStatusName;

    @ApiModelProperty("管理员备注")
    private String adminRemark;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("订单商品列表")
    private List<OrderItemVO> items;
}
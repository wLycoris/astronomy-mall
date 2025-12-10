package com.astronomy.mall.module.order.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;

    // 收货信息
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String fullAddress; // 完整地址

    // 价格信息
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal discountAmount;
    private BigDecimal paymentAmount;

    // 订单状态
    private Integer status;
    private String statusText; // 状态文本
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;

    // 备注信息
    private String remark;
    private String cancelReason;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 订单商品列表
    private List<OrderItemVO> items;
}
package com.astronomy.mall.module.order.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单详情视图对象
 */
@Data
public class OrderItemVO {

    private Long id;
    private Long orderId;
    private Long productId;

    // 商品快照
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private String productBrand;

    // 购买信息
    private Integer quantity;
    private BigDecimal totalPrice;
}
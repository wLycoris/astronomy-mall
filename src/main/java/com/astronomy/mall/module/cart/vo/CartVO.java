package com.astronomy.mall.module.cart.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车视图对象
 */
@Data
public class CartVO {

    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer selected;
    private LocalDateTime createTime;

    // 关联商品信息
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private String productBrand;
    private Integer stock;

    // 计算字段
    private BigDecimal subtotal; // 小计 = 单价 * 数量
}
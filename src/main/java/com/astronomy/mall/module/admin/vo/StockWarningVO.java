package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * 📌 库存预警VO
 */
@Data
public class StockWarningVO {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 主图
     */
    private String mainImage;

    /**
     * 当前库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 预警等级(1-低库存<10, 2-缺货=0)
     */
    private Integer warningLevel;

    /**
     * 预警描述
     */
    private String warningDesc;

    /**
     * 分类名称
     */
    private String categoryName;
}
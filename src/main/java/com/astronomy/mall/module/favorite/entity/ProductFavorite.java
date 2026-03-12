package com.astronomy.mall.module.favorite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品收藏实体类
 *
 * 📌 对应数据库表: tb_product_favorite
 * 📌 关联关系:
 *   - user_id → tb_user.id
 *   - product_id → tb_product.id
 * 📌 冗余字段说明:
 *   - product_name / product_price / product_image 冗余存储商品基本信息，
 *     避免每次查收藏列表都 JOIN 商品表，提升查询性能。
 *     商品被删除后，收藏列表仍能显示商品名和图片（但会标注"已下架"）。
 *
 * 建表SQL:
 * CREATE TABLE `tb_product_favorite` (
 *   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
 *   `user_id` bigint(20) NOT NULL COMMENT '用户ID',
 *   `product_id` bigint(20) NOT NULL COMMENT '商品ID',
 *   `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称(冗余)',
 *   `product_price` decimal(10,2) DEFAULT NULL COMMENT '收藏时价格',
 *   `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片(冗余)',
 *   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
 *   PRIMARY KEY (`id`),
 *   UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
 *   KEY `idx_user_id` (`user_id`),
 *   KEY `idx_product_id` (`product_id`)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';
 */
@Data
@TableName("tb_product_favorite")
public class ProductFavorite {

    /** 收藏ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /**
     * 商品名称（冗余字段）
     * 商品删除后，收藏列表仍能展示商品名称
     */
    private String productName;

    /**
     * 收藏时的商品价格（冗余字段）
     * 用于在收藏列表展示收藏时的价格，也可与当前价格对比判断是否降价
     */
    private BigDecimal productPrice;

    /**
     * 商品主图（冗余字段）
     * 商品删除后，收藏列表仍能展示商品图片
     */
    private String productImage;

    /** 收藏时间 */
    private LocalDateTime createTime;
}
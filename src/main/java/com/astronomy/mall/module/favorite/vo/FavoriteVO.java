package com.astronomy.mall.module.favorite.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的收藏列表 VO
 *
 * 📌 前端展示字段说明:
 *   - productImage    商品图片（优先取实时商品表，无则取冗余值）
 *   - productName     商品名称（优先取实时商品表，无则取冗余值）
 *   - currentPrice    商品当前价格（实时取商品表）
 *   - favoritePrice   收藏时的价格（冗余字段，用于降价对比）
 *   - isOffShelf      是否下架/删除 (true=商品已下架或被删除)
 *   - isPriceDown     是否降价 (当前价格 < 收藏时价格)
 *   - createTime      收藏时间
 *
 * 📌 下架判断逻辑:
 *   如果 tb_product 中查不到该商品（deleted=1 或 status=0）则 isOffShelf=true
 */
@Data
public class FavoriteVO {

    /** 收藏记录ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品图片（实时取 tb_product，商品删除后用冗余值） */
    private String productImage;

    /** 商品名称（实时取 tb_product，商品删除后用冗余值） */
    private String productName;

    /** 商品当前价格（实时取 tb_product，商品下架/删除后为 null） */
    private BigDecimal currentPrice;

    /** 收藏时的价格（冗余字段） */
    private BigDecimal favoritePrice;

    /**
     * 是否已下架 / 删除
     * true  → 商品已被删除或已下架（status=0），列表展示"已下架"标签
     * false → 商品正常上架中
     */
    private Boolean isOffShelf;

    /**
     * 是否降价
     * true  → 当前价格 < 收藏时价格，提示用户"价格已降低"
     * false → 未降价或商品已下架
     */
    private Boolean isPriceDown;

    /**
     * 是否涨价
     * true  → 当前价格 > 收藏时价格
     * false → 未涨价或商品已下架
     */
    private Boolean isPriceUp;

    /**
     * 收藏时间
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
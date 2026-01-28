package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品导入DTO
 * 用于Excel导入时的数据映射
 *
 * 路径: com.astronomy.mall.module.admin.dto.ProductImportDTO
 */
@Data
public class ProductImportDTO {

    /**
     * 商品名称 (必填)
     */
    private String productName;

    /**
     * 分类ID (必填)
     */
    private Long categoryId;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 价格 (必填)
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存 (必填)
     */
    private Integer stock;

    /**
     * 主图URL (必填)
     */
    private String mainImage;

    /**
     * 商品图片(多张,逗号分隔)
     */
    private String images;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 规格参数(JSON格式)
     */
    private String specifications;

    /**
     * 搜索关键词
     */
    private String keywords;

    /**
     * 商品标签(JSON数组)
     */
    private String tags;

    /**
     * 是否推荐(0-否 1-是)
     */
    private Integer isRecommend;

    /**
     * 是否热卖(0-否 1-是)
     */
    private Integer isHot;

    /**
     * 是否新品(0-否 1-是)
     */
    private Integer isNew;
}
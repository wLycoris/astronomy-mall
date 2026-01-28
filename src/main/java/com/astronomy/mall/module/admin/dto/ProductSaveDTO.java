package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 商品新增/编辑DTO
 */
@Data
public class ProductSaveDTO {

    /**
     * 商品ID (编辑时必传)
     */
    private Long id;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称不能超过200个字符")
    private String productName;

    /**
     * 副标题
     */
    @Size(max = 300, message = "副标题不能超过300个字符")
    private String subTitle;

    /**
     * 品牌
     */
    @Size(max = 100, message = "品牌不能超过100个字符")
    private String brand;

    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
    private BigDecimal price;

    /**
     * 原价
     */
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    /**
     * 主图URL
     */
    @NotBlank(message = "主图不能为空")
    private String mainImage;

    /**
     * 商品图片(多张,逗号分隔)
     */
    private String images;

    /**
     * 商品详情(富文本)
     */
    private String detail;

    /**
     * 规格参数(JSON格式)
     */
    private String specifications;

    /**
     * 商品标签(JSON数组)
     */
    private String tags;

    /**
     * 搜索关键词
     */
    private String keywords;

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
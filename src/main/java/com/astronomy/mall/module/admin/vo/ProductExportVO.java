package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品导出VO
 * 用于Excel导出时的数据展示
 *
 * 路径: com.astronomy.mall.module.admin.vo.ProductExportVO
 */
@Data
public class ProductExportVO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 主图URL
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
     * 规格参数
     */
    private String specifications;

    /**
     * 搜索关键词
     */
    private String keywords;

    /**
     * 商品标签
     */
    private String tags;

    /**
     * 状态(0-下架 1-上架)
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

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

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
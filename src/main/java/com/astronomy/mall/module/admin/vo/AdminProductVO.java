package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 📌 商品列表VO (后台管理用) - 完整版
 * 包含所有字段，支持编辑时数据回显
 */
@Data
public class AdminProductVO {

    // =============================================
    // 基本信息
    // =============================================

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称 (仅用于列表显示)
     */
    private String categoryName;

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
     * 商品状态(0-下架 1-上架)
     */
    private Integer status;

    /**
     * 状态描述 (仅用于列表显示)
     */
    private String statusDesc;

    /**
     * 是否推荐
     */
    private Integer isRecommend;

    /**
     * 是否热卖
     */
    private Integer isHot;

    /**
     * 是否新品
     */
    private Integer isNew;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 搜索关键词
     */
    private String keywords;

    // =============================================
    // 图片信息 (Tab 2)
    // =============================================

    /**
     * 商品图片 (多张,逗号分隔)
     * 🔥 编辑时需要回显到"图片信息" tab
     */
    private String images;

    // =============================================
    // 详细信息 (Tab 3)
    // =============================================

    /**
     * 商品详情 (富文本HTML)
     * 🔥 编辑时需要回显到"详细信息" tab
     */
    private String detail;

    /**
     * 规格参数 (JSON格式)
     * 🔥 编辑时需要回显到"详细信息" tab
     */
    private String specifications;

    /**
     * 商品标签 (JSON数组)
     * 🔥 编辑时需要回显到"详细信息" tab
     * 示例: ["天文望远镜","入门级","便携式"]
     */
    private String tags;

    // =============================================
    // 时间信息
    // =============================================

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
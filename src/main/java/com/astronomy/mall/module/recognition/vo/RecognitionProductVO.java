package com.astronomy.mall.module.recognition.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 识别推荐商品 VO（轻量级）
 *
 * 📌 设计说明:
 *   recognition 模块不直接依赖 product 模块的 VO，
 *   此处定义一个精简的商品VO，仅包含推荐卡片所需字段。
 *
 * 📌 数据来源: tb_product 表
 *
 * @since 4.4 (2026-03-16)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionProductVO {

    /** 商品ID */
    private Long id;

    /** 商品名称 */
    private String productName;

    /** 主图URL */
    private String mainImage;

    /** 当前价格 */
    private BigDecimal price;

    /**
     * 推荐理由
     * 示例: "适合深空摄影" / "适合行星观测"
     * 📌 由后端根据匹配的标签关键词生成
     */
    private String reason;

    /** 销量（用于兜底排序） */
    private Integer salesCount;
}
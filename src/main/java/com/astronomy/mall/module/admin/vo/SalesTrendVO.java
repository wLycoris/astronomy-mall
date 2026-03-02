package com.astronomy.mall.module.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售趋势 VO
 *
 * 接口: GET /api/admin/statistics/sales-trend?days=7|30
 */
@Data
public class SalesTrendVO {

    /** 日期列表 (如: ["2026-02-24", ..., "2026-03-02"]) */
    private List<String> dates;

    /** 每日订单数 */
    private List<Integer> orderCounts;

    /** 每日销售额 */
    private List<BigDecimal> salesAmounts;

    // ===== 商品销售排行 (复用在同一接口返回, 避免多次请求) =====
    /** 商品销售排行 (Top 10) */
    private List<ProductRankVO> productRank;

    /** 分类销售占比 */
    private List<CategorySalesVO> categorySales;

    @Data
    public static class ProductRankVO {
        private Long productId;
        private String productName;
        private String productImage;
        /** 销售数量 */
        private Integer salesCount;
        /** 销售金额 */
        private BigDecimal salesAmount;
    }

    @Data
    public static class CategorySalesVO {
        private Long categoryId;
        private String categoryName;
        /** 销售数量 */
        private Integer salesCount;
        /** 销售金额 */
        private BigDecimal salesAmount;
        /** 占比 (百分比, 如 35.6) */
        private Double percentage;
    }
}
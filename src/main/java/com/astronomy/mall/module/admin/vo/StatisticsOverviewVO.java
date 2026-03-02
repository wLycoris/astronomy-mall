package com.astronomy.mall.module.admin.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 数据概览 VO
 *
 * 接口: GET /api/admin/statistics/overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsOverviewVO {

    // ===== 今日数据 =====
    /** 今日订单数 */
    private Integer todayOrderCount;

    /** 今日销售额 */
    private BigDecimal todaySalesAmount;

    // ===== 本月数据 =====
    /** 本月订单数 */
    private Integer monthOrderCount;

    /** 本月销售额 */
    private BigDecimal monthSalesAmount;

    // ===== 待处理数据 =====
    /** 待处理订单数 (status=1 待发货) */
    private Integer pendingOrderCount;

    /** 待审核退款数 (refund_status=0) */
    private Integer pendingRefundCount;

    /** 库存预警数 (stock <= warning_stock) */
    private Integer stockWarningCount;

    // ===== 对比数据（与昨日/上月对比，用于趋势箭头） =====
    /** 今日订单数与昨日对比百分比 (正数=增长, 负数=下降) */
    private Double todayOrderGrowth;

    /** 今日销售额与昨日对比百分比 */
    private Double todaySalesGrowth;

    /** 本月销售额与上月对比百分比 */
    private Double monthSalesGrowth;
}
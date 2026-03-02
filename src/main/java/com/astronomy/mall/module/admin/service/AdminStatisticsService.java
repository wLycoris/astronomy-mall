package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.vo.*;

/**
 * 数据统计 Service
 */
public interface AdminStatisticsService {

    /**
     * 数据概览（今日/本月订单+销售额、待处理数量）
     */
    StatisticsOverviewVO getOverview();

    /**
     * 销售趋势 + 商品排行 + 分类占比
     *
     * @param days 天数 (7/30/90), 默认 7
     */
    SalesTrendVO getSalesTrend(int days);

    /**
     * 订单统计（状态分布 + 金额分布）
     */
    OrderStatisticsVO getOrderStatistics();

    /**
     * 用户统计（新增趋势 + 活跃度 + 地区分布 + 等级分布）
     *
     * @param days 天数 (7/30), 默认 7
     */
    UserStatisticsVO getUserStatistics(int days);

    /**
     * 评价统计（评分分布 + 评价趋势）
     *
     * @param days 天数 (7/30), 默认 7
     */
    ReviewStatisticsVO getReviewStatistics(int days);
}
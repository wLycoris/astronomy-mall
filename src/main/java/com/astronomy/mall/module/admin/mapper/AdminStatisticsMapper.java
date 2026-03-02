package com.astronomy.mall.module.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 数据统计 Mapper
 *
 * 📌 所有统计SQL均写在 AdminStatisticsMapper.xml 中
 */
@Mapper
public interface AdminStatisticsMapper {

    // ===================== 数据概览 =====================

    /** 今日订单数 */
    Integer getTodayOrderCount();

    /** 今日销售额 (已支付订单的实付金额之和) */
    BigDecimal getTodaySalesAmount();

    /** 昨日订单数 */
    Integer getYesterdayOrderCount();

    /** 昨日销售额 */
    BigDecimal getYesterdaySalesAmount();

    /** 本月订单数 */
    Integer getMonthOrderCount();

    /** 本月销售额 */
    BigDecimal getMonthSalesAmount();

    /** 上月销售额 */
    BigDecimal getLastMonthSalesAmount();

    /** 待处理订单数 (status=1 待发货) */
    Integer getPendingOrderCount();

    /** 待审核退款数 (status=0) */
    Integer getPendingRefundCount();

    /** 库存预警数 */
    Integer getStockWarningCount();

    // ===================== 销售趋势 =====================

    /**
     * 按日查询最近N天的订单数和销售额
     * @param days 天数
     * @return list of {date, order_count, sales_amount}
     */
    List<Map<String, Object>> getSalesTrend(@Param("days") int days);

    /**
     * 商品销售排行 Top 10
     * @param days 统计天数 (7/30/90)
     */
    List<Map<String, Object>> getProductRank(@Param("days") int days);

    /**
     * 分类销售占比
     * @param days 统计天数
     */
    List<Map<String, Object>> getCategorySales(@Param("days") int days);

    // ===================== 订单统计 =====================

    /** 订单状态分布 */
    List<Map<String, Object>> getOrderStatusDistribution();

    /** 订单金额分布 (按区间) */
    List<Map<String, Object>> getOrderAmountDistribution();

    // ===================== 用户统计 =====================

    /**
     * 按日查询最近N天新增用户数
     * @param days 天数
     */
    List<Map<String, Object>> getUserTrend(@Param("days") int days);

    /** 活跃用户数 (近30天有登录记录) */
    Integer getActiveUserCount();

    /** 总用户数 */
    Integer getTotalUserCount();

    /** 省份分布 Top 10 */
    List<Map<String, Object>> getProvinceDistribution();

    /** 用户等级分布 */
    List<Map<String, Object>> getLevelDistribution();

    // ===================== 评价统计 =====================

    /** 评分分布 (1-5星各多少条) */
    List<Map<String, Object>> getRatingDistribution();

    /** 总评价数 */
    Integer getTotalReviewCount();

    /** 平均评分 */
    Double getAvgRating();

    /**
     * 按日查询最近N天新增评价数
     * @param days 天数
     */
    List<Map<String, Object>> getReviewTrend(@Param("days") int days);
}
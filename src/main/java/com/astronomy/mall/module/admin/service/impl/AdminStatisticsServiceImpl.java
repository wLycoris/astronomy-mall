package com.astronomy.mall.module.admin.service.impl;

import cn.hutool.core.convert.Convert;
import com.astronomy.mall.module.admin.mapper.AdminStatisticsMapper;
import com.astronomy.mall.module.admin.service.AdminStatisticsService;
import com.astronomy.mall.module.admin.vo.*;
import com.astronomy.mall.module.admin.vo.ReviewStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 数据统计 ServiceImpl
 *
 * ⚠️ 避坑指南：
 * - MySQL selectMaps 返回的 BigDecimal 字段需用 ((BigDecimal) obj).doubleValue() 或 Convert.toDouble()
 * - tinyint 字段在 selectMaps 中会被 JDBC 映射为 Boolean，
 *   因此 Mapper XML 中对 tinyint 字段统一使用 CAST(field AS UNSIGNED)
 * - status、rating 等 Integer 字段直接 Convert.toInt() 即可
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final AdminStatisticsMapper statisticsMapper;

    // ==================== 等级名称常量 ====================
    private static final Map<Integer, String> LEVEL_NAMES = new LinkedHashMap<>();
    static {
        LEVEL_NAMES.put(1, "入门");
        LEVEL_NAMES.put(2, "初级");
        LEVEL_NAMES.put(3, "中级");
        LEVEL_NAMES.put(4, "高级");
        LEVEL_NAMES.put(5, "专家");
    }

    // ==================== 数据概览 ====================

    @Override
    public StatisticsOverviewVO getOverview() {
        // 今日数据
        Integer todayOrderCount    = statisticsMapper.getTodayOrderCount();
        BigDecimal todaySales      = statisticsMapper.getTodaySalesAmount();

        // 昨日数据（用于计算增长率）
        Integer yesterdayOrderCount = statisticsMapper.getYesterdayOrderCount();
        BigDecimal yesterdaySales   = statisticsMapper.getYesterdaySalesAmount();

        // 本月数据
        Integer monthOrderCount    = statisticsMapper.getMonthOrderCount();
        BigDecimal monthSales      = statisticsMapper.getMonthSalesAmount();
        BigDecimal lastMonthSales  = statisticsMapper.getLastMonthSalesAmount();

        // 待处理
        Integer pendingOrder  = statisticsMapper.getPendingOrderCount();
        Integer pendingRefund = statisticsMapper.getPendingRefundCount();
        Integer stockWarning  = statisticsMapper.getStockWarningCount();

        return StatisticsOverviewVO.builder()
                .todayOrderCount(todayOrderCount)
                .todaySalesAmount(todaySales)
                .monthOrderCount(monthOrderCount)
                .monthSalesAmount(monthSales)
                .pendingOrderCount(pendingOrder)
                .pendingRefundCount(pendingRefund)
                .stockWarningCount(stockWarning)
                .todayOrderGrowth(calcGrowthRate(yesterdayOrderCount, todayOrderCount))
                .todaySalesGrowth(calcGrowthRate(yesterdaySales, todaySales))
                .monthSalesGrowth(calcGrowthRate(lastMonthSales, monthSales))
                .build();
    }

    // ==================== 销售趋势 ====================

    @Override
    public SalesTrendVO getSalesTrend(int days) {
        // 限制合法范围
        days = limitDays(days, 7, new int[]{7, 30, 90});

        List<Map<String, Object>> trendList = statisticsMapper.getSalesTrend(days);
        List<Map<String, Object>> rankList  = statisticsMapper.getProductRank(days);
        List<Map<String, Object>> catList   = statisticsMapper.getCategorySales(days);

        SalesTrendVO vo = new SalesTrendVO();

        // ---- 趋势折线图 ----
        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> salesAmounts = new ArrayList<>();

        for (Map<String, Object> row : trendList) {
            dates.add(Convert.toStr(row.get("date")));
            orderCounts.add(Convert.toInt(row.get("order_count"), 0));
            Object amtObj = row.get("sales_amount");
            BigDecimal amt = amtObj == null ? BigDecimal.ZERO : new BigDecimal(amtObj.toString());
            salesAmounts.add(amt);
        }
        vo.setDates(dates);
        vo.setOrderCounts(orderCounts);
        vo.setSalesAmounts(salesAmounts);

        // ---- 商品排行 ----
        List<SalesTrendVO.ProductRankVO> productRank = new ArrayList<>();
        for (Map<String, Object> row : rankList) {
            SalesTrendVO.ProductRankVO item = new SalesTrendVO.ProductRankVO();
            item.setProductId(Convert.toLong(row.get("product_id")));
            item.setProductName(Convert.toStr(row.get("product_name")));
            item.setProductImage(Convert.toStr(row.get("product_image")));
            item.setSalesCount(Convert.toInt(row.get("sales_count"), 0));
            Object amtObj = row.get("sales_amount");
            item.setSalesAmount(amtObj == null ? BigDecimal.ZERO : new BigDecimal(amtObj.toString()));
            productRank.add(item);
        }
        vo.setProductRank(productRank);

        // ---- 分类占比 ----
        // 先算总销售额，用于计算百分比
        BigDecimal totalCatSales = catList.stream()
                .map(r -> {
                    Object a = r.get("sales_amount");
                    return a == null ? BigDecimal.ZERO : new BigDecimal(a.toString());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesTrendVO.CategorySalesVO> categorySales = new ArrayList<>();
        for (Map<String, Object> row : catList) {
            SalesTrendVO.CategorySalesVO item = new SalesTrendVO.CategorySalesVO();
            item.setCategoryId(Convert.toLong(row.get("category_id")));
            item.setCategoryName(Convert.toStr(row.get("category_name")));
            item.setSalesCount(Convert.toInt(row.get("sales_count"), 0));
            Object amtObj = row.get("sales_amount");
            BigDecimal amt = amtObj == null ? BigDecimal.ZERO : new BigDecimal(amtObj.toString());
            item.setSalesAmount(amt);
            double pct = totalCatSales.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : amt.multiply(BigDecimal.valueOf(100))
                    .divide(totalCatSales, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            item.setPercentage(pct);
            categorySales.add(item);
        }
        vo.setCategorySales(categorySales);

        return vo;
    }

    // ==================== 订单统计 ====================

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        List<Map<String, Object>> statusList = statisticsMapper.getOrderStatusDistribution();
        List<Map<String, Object>> amountList = statisticsMapper.getOrderAmountDistribution();

        OrderStatisticsVO vo = new OrderStatisticsVO();

        // ---- 状态分布 ----
        // 订单状态: 0待支付 1待发货 2待收货 3已完成 4已取消
        int[] statusCounts = new int[5];
        int totalOrders = 0;
        for (Map<String, Object> row : statusList) {
            int status = Convert.toInt(row.get("status"), -1);
            int cnt = Convert.toInt(row.get("cnt"), 0);
            if (status >= 0 && status <= 4) {
                statusCounts[status] = cnt;
                totalOrders += cnt;
            }
        }
        vo.setWaitPayCount(statusCounts[0]);
        vo.setWaitDeliverCount(statusCounts[1]);
        vo.setWaitReceiveCount(statusCounts[2]);
        vo.setCompletedCount(statusCounts[3]);
        vo.setCancelledCount(statusCounts[4]);

        // ---- 金额分布 ----
        int totalForPct = amountList.stream().mapToInt(r -> Convert.toInt(r.get("cnt"), 0)).sum();
        List<OrderStatisticsVO.AmountRangeVO> amountDist = new ArrayList<>();
        for (Map<String, Object> row : amountList) {
            OrderStatisticsVO.AmountRangeVO item = new OrderStatisticsVO.AmountRangeVO();
            item.setLabel(Convert.toStr(row.get("label")));
            int cnt = Convert.toInt(row.get("cnt"), 0);
            item.setCount(cnt);
            double pct = totalForPct == 0 ? 0.0
                    : Math.round(cnt * 10000.0 / totalForPct) / 100.0;
            item.setPercentage(pct);
            amountDist.add(item);
        }
        vo.setAmountDistribution(amountDist);

        return vo;
    }

    // ==================== 用户统计 ====================

    @Override
    public UserStatisticsVO getUserStatistics(int days) {
        days = limitDays(days, 7, new int[]{7, 30});

        List<Map<String, Object>> trendList   = statisticsMapper.getUserTrend(days);
        List<Map<String, Object>> provinceList = statisticsMapper.getProvinceDistribution();
        List<Map<String, Object>> levelList   = statisticsMapper.getLevelDistribution();
        Integer activeCount = statisticsMapper.getActiveUserCount();
        Integer totalCount  = statisticsMapper.getTotalUserCount();

        UserStatisticsVO vo = new UserStatisticsVO();

        // ---- 新增用户趋势 ----
        List<String> dates = new ArrayList<>();
        List<Integer> newUserCounts = new ArrayList<>();
        for (Map<String, Object> row : trendList) {
            dates.add(Convert.toStr(row.get("date")));
            newUserCounts.add(Convert.toInt(row.get("new_user_count"), 0));
        }
        vo.setDates(dates);
        vo.setNewUserCounts(newUserCounts);

        // ---- 活跃度 ----
        vo.setActiveUserCount(activeCount);
        vo.setTotalUserCount(totalCount);
        double activeRate = (totalCount == null || totalCount == 0) ? 0.0
                : Math.round((activeCount == null ? 0 : activeCount) * 10000.0 / totalCount) / 100.0;
        vo.setActiveRate(activeRate);

        // ---- 省份分布 ----
        int totalUsers = provinceList.stream().mapToInt(r -> Convert.toInt(r.get("user_count"), 0)).sum();
        List<UserStatisticsVO.ProvinceDistributionVO> provinceDist = new ArrayList<>();
        for (Map<String, Object> row : provinceList) {
            UserStatisticsVO.ProvinceDistributionVO item = new UserStatisticsVO.ProvinceDistributionVO();
            item.setProvince(Convert.toStr(row.get("province")));
            int cnt = Convert.toInt(row.get("user_count"), 0);
            item.setUserCount(cnt);
            item.setPercentage(totalUsers == 0 ? 0.0 : Math.round(cnt * 10000.0 / totalUsers) / 100.0);
            provinceDist.add(item);
        }
        vo.setProvinceDistribution(provinceDist);

        // ---- 等级分布 ----
        int totalForLevel = levelList.stream().mapToInt(r -> Convert.toInt(r.get("user_count"), 0)).sum();
        List<UserStatisticsVO.LevelDistributionVO> levelDist = new ArrayList<>();
        for (Map<String, Object> row : levelList) {
            UserStatisticsVO.LevelDistributionVO item = new UserStatisticsVO.LevelDistributionVO();
            // ⚠️ XML中已 CAST AS UNSIGNED，这里取别名 level_val
            int level = Convert.toInt(row.get("level_val"), 1);
            item.setLevel(level);
            item.setLevelName(LEVEL_NAMES.getOrDefault(level, "未知"));
            int cnt = Convert.toInt(row.get("user_count"), 0);
            item.setUserCount(cnt);
            item.setPercentage(totalForLevel == 0 ? 0.0 : Math.round(cnt * 10000.0 / totalForLevel) / 100.0);
            levelDist.add(item);
        }
        vo.setLevelDistribution(levelDist);

        return vo;
    }

    // ==================== 评价统计 ====================

    @Override
    public ReviewStatisticsVO getReviewStatistics(int days) {
        days = limitDays(days, 7, new int[]{7, 30});

        List<Map<String, Object>> ratingList = statisticsMapper.getRatingDistribution();
        Integer totalCount = statisticsMapper.getTotalReviewCount();
        Double avgRating   = statisticsMapper.getAvgRating();
        List<Map<String, Object>> trendList = statisticsMapper.getReviewTrend(days);

        ReviewStatisticsVO vo = new ReviewStatisticsVO();
        vo.setTotalCount(totalCount);
        vo.setAvgRating(avgRating == null ? 0.0 : Math.round(avgRating * 100.0) / 100.0);

        // ---- 评分分布 ----
        int goodCount = 0; // 4-5星
        List<ReviewStatisticsVO.RatingDistributionVO> ratingDist = new ArrayList<>();
        for (Map<String, Object> row : ratingList) {
            ReviewStatisticsVO.RatingDistributionVO item = new ReviewStatisticsVO.RatingDistributionVO();
            int rating = Convert.toInt(row.get("rating"), 0);
            int cnt = Convert.toInt(row.get("cnt"), 0);
            item.setRating(rating);
            item.setCount(cnt);
            item.setPercentage(totalCount == null || totalCount == 0 ? 0.0
                    : Math.round(cnt * 10000.0 / totalCount) / 100.0);
            ratingDist.add(item);
            if (rating >= 4) goodCount += cnt;
        }
        vo.setRatingDistribution(ratingDist);

        // 好评率
        double goodRate = (totalCount == null || totalCount == 0) ? 0.0
                : Math.round(goodCount * 10000.0 / totalCount) / 100.0;
        vo.setGoodRate(goodRate);

        // ---- 评价趋势 ----
        List<String> dates = new ArrayList<>();
        List<Integer> reviewCounts = new ArrayList<>();
        for (Map<String, Object> row : trendList) {
            dates.add(Convert.toStr(row.get("date")));
            reviewCounts.add(Convert.toInt(row.get("review_count"), 0));
        }
        vo.setDates(dates);
        vo.setReviewCounts(reviewCounts);

        return vo;
    }

    // ==================== 私有工具方法 ====================

    /**
     * 计算增长率 (%)
     * 例：昨日100，今日120 → +20.0
     *     昨日100，今日80  → -20.0
     *     昨日0，今日10   → +100.0 (视为全部新增)
     *     昨日0，今日0    → 0.0
     */
    private Double calcGrowthRate(Number oldValue, Number newValue) {
        if (oldValue == null || newValue == null) return 0.0;
        double oldD = oldValue.doubleValue();
        double newD = newValue.doubleValue();
        if (oldD == 0) return newD > 0 ? 100.0 : 0.0;
        double rate = (newD - oldD) / oldD * 100;
        return Math.round(rate * 100.0) / 100.0;
    }

    /**
     * BigDecimal 重载版
     */
    private Double calcGrowthRate(BigDecimal oldValue, BigDecimal newValue) {
        BigDecimal old = oldValue == null ? BigDecimal.ZERO : oldValue;
        BigDecimal nv  = newValue  == null ? BigDecimal.ZERO : newValue;
        return calcGrowthRate((Number) old.doubleValue(), (Number) nv.doubleValue());
    }

    /**
     * 限制 days 在合法候选值中，否则取默认值
     */
    private int limitDays(int days, int defaultVal, int[] allowed) {
        for (int a : allowed) {
            if (days == a) return days;
        }
        return defaultVal;
    }
}
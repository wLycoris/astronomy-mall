package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.service.AdminStatisticsService;
import com.astronomy.mall.module.admin.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 数据统计 Controller
 *
 * 接口列表：
 *   GET /api/admin/statistics/overview      - 数据概览
 *   GET /api/admin/statistics/sales-trend   - 销售趋势+商品排行+分类占比
 *   GET /api/admin/statistics/order-status  - 订单统计
 *   GET /api/admin/statistics/user-trend    - 用户统计
 *   GET /api/admin/statistics/review        - 评价统计
 *
 * 📌 全部接口需要管理员权限（AdminInterceptor 拦截）
 */
@Api(tags = "后台-数据统计")
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService statisticsService;

    /**
     * 数据概览
     * 包含：今日/本月订单数和销售额、待处理订单数、待审核退款数、库存预警数
     */
    @ApiOperation("数据概览")
    @GetMapping("/overview")
    public Result<StatisticsOverviewVO> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    /**
     * 销售趋势 + 商品销售排行(Top10) + 分类销售占比
     *
     * @param days 统计天数，支持 7 / 30 / 90，默认 7
     */
    @ApiOperation("销售趋势")
    @GetMapping("/sales-trend")
    public Result<SalesTrendVO> getSalesTrend(
            @ApiParam("统计天数(7/30/90)") @RequestParam(defaultValue = "7") int days) {
        return Result.success(statisticsService.getSalesTrend(days));
    }

    /**
     * 订单统计
     * 包含：订单状态分布、订单金额区间分布
     */
    @ApiOperation("订单统计")
    @GetMapping("/order-status")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        return Result.success(statisticsService.getOrderStatistics());
    }

    /**
     * 用户统计
     * 包含：新增用户趋势、活跃度、省份分布、等级分布
     *
     * @param days 统计天数，支持 7 / 30，默认 7
     */
    @ApiOperation("用户统计")
    @GetMapping("/user-trend")
    public Result<UserStatisticsVO> getUserStatistics(
            @ApiParam("统计天数(7/30)") @RequestParam(defaultValue = "7") int days) {
        return Result.success(statisticsService.getUserStatistics(days));
    }

    /**
     * 评价统计
     * 包含：评分分布、好评率、评价趋势
     *
     * @param days 统计天数，支持 7 / 30，默认 7
     */
    @ApiOperation("评价统计")
    @GetMapping("/review")
    public Result<ReviewStatisticsVO> getReviewStatistics(
            @ApiParam("统计天数(7/30)") @RequestParam(defaultValue = "7") int days) {
        return Result.success(statisticsService.getReviewStatistics(days));
    }
}
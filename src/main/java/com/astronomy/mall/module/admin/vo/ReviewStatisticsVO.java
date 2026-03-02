package com.astronomy.mall.module.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * 评价统计 VO
 *
 * 复用在 /api/admin/statistics/overview 接口, 不单独提供接口
 * 也可在 sales-trend 接口中一并返回
 */
@Data
public class ReviewStatisticsVO {

    /** 评分分布（1-5星各有多少条） */
    private List<RatingDistributionVO> ratingDistribution;

    /** 总评价数 */
    private Integer totalCount;

    /** 平均评分 */
    private Double avgRating;

    /** 好评率 (4-5星占比, 百分比) */
    private Double goodRate;

    /** 日期列表 */
    private List<String> dates;

    /** 每日新增评价数 */
    private List<Integer> reviewCounts;

    @Data
    public static class RatingDistributionVO {
        /** 星级(1-5) */
        private Integer rating;
        private Integer count;
        private Double percentage;
    }
}
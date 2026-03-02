package com.astronomy.mall.module.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户统计 VO
 *
 * 接口: GET /api/admin/statistics/user-trend?days=7|30
 */
@Data
public class UserStatisticsVO {

    // ===== 新增用户趋势 =====
    /** 日期列表 */
    private List<String> dates;

    /** 每日新增用户数 */
    private List<Integer> newUserCounts;

    // ===== 用户活跃度（近30天有登录记录的用户数） =====
    /** 活跃用户数（近30天） */
    private Integer activeUserCount;
    /** 总用户数 */
    private Integer totalUserCount;
    /** 活跃率 (百分比) */
    private Double activeRate;

    // ===== 用户地区分布（Top 10 省份） =====
    private List<ProvinceDistributionVO> provinceDistribution;

    // ===== 用户等级分布 =====
    private List<LevelDistributionVO> levelDistribution;

    @Data
    public static class ProvinceDistributionVO {
        private String province;
        private Integer userCount;
        private Double percentage;
    }

    @Data
    public static class LevelDistributionVO {
        /** 等级(1-5) */
        private Integer level;
        /** 等级名称 */
        private String levelName;
        private Integer userCount;
        private Double percentage;
    }
}
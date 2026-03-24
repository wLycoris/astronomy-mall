package com.astronomy.mall.module.location.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 观测点列表 VO（附近搜索接口返回，不含天气，天气按需懒加载）
 *
 * 对应接口: GET /api/location/spots
 * 注意:
 *   - weatherSummary 字段故意不包含，天气由前端调 /api/location/weather 懒加载
 *   - distance 单位为 km，由 Haversine 公式在 Mapper 层计算
 *   - myScore: 当前登录用户给本观测点的评分（未评分为 null，未登录也为 null）
 *
 * 数据库: tb_observation_spot
 */
@Data
public class ObservationSpotVO {

    /** 观测点ID */
    private Long id;

    /** 观测点名称 */
    private String name;

    /** 所在省份 */
    private String province;

    /** 所在城市 */
    private String city;

    /** 详细地址 */
    private String address;

    /** 纬度 */
    private BigDecimal latitude;

    /** 经度 */
    private BigDecimal longitude;

    /** 海拔（米） */
    private Integer altitude;

    /**
     * 鲍特尔暗天等级 (1-9)
     * 1=最佳暗天(银河清晰可见), 9=城市天空(只能看到月亮/金星)
     * 越小越暗越适合观测
     */
    private Integer bortleLevel;

    /** 综合评分 (1.0-5.0，保留1位小数) */
    private BigDecimal rating;

    /** 评分人数 */
    private Integer ratingCount;

    /** 简介（列表页简短描述） */
    private String description;

    /** 主图URL（第一张图片） */
    private String mainImage;

    /**
     * 与当前查询坐标的距离（km，保留2位小数）
     * 由 Haversine 公式在 SQL 中计算
     */
    private Double distance;

    /**
     * 当前登录用户对此观测点的历史评分（1-5）
     * 未评分或未登录时为 null
     * 前端用此字段判断是否显示"已评价"禁用状态
     */
    private Integer myScore;

    /** 创建时间 */
    private LocalDateTime createTime;
}
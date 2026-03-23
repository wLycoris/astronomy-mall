package com.astronomy.mall.module.location.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 观测点详情VO（含当前用户评分状态）
 * 对应接口: GET /api/location/spot/{id}
 *
 * 📌 6.0 骨架，字段在 6.1 节按需调整
 * 📌 myScore: 当前登录用户对该观测点的历史评分（null=未评分，前端据此显示评分UI）
 */
@Data
public class SpotDetailVO {

    /** 观测点ID */
    private Long id;

    /** 观测点名称 */
    private String spotName;

    /** 经度(高德GCJ-02) */
    private BigDecimal longitude;

    /** 纬度(高德GCJ-02) */
    private BigDecimal latitude;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 详细地址 */
    private String address;

    /** 海拔(米) */
    private Integer altitude;

    /** Bortle暗天等级(1-9，越小越好) */
    private Integer lightPollutionLevel;

    /** 综合评分(0-5) */
    private BigDecimal rating;

    /** 评分人数 */
    private Integer ratingCount;

    /** 描述 */
    private String description;

    /** 图片列表（JSON数组字符串） */
    private String images;

    /** 历史签到总次数 */
    private Integer checkinCount;

    /**
     * 当前登录用户对此观测点的历史评分（null=未评分过）
     * 用于前端判断是否显示"已评分"状态
     */
    private Integer myScore;

    /**
     * 当前用户今日是否已签到（true=已签到，签到按钮置灰）
     */
    private Boolean todayCheckedIn;
}
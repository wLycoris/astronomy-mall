package com.astronomy.mall.module.location.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 观测点列表/地图展示VO
 * 对应接口: GET /api/location/spots
 *
 * 📌 6.0 骨架，字段在 6.1 节按需调整
 * 📌 images 返回JSON字符串，前端自行 JSON.parse 转数组
 */
@Data
public class ObservationSpotVO {

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

    /**
     * Bortle暗天等级(1-9，越小越好)
     * 前端根据此值显示颜色标签:
     *   1-2 = 极佳（绿色）
     *   3-4 = 优良（蓝色）
     *   5-6 = 一般（黄色）
     *   7-9 = 差（红色）
     */
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
}
package com.astronomy.mall.module.location.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 观测点详情 VO
 *
 * 对应接口: GET /api/location/spot/{id}
 *
 * ⚠️ 字段说明（与实体类对照）:
 *   name         ← entity.spotName        (spot_name)
 *   bortleLevel  ← entity.lightPollutionLevel (light_pollution_level)
 *   fullDescription ← 复用 description 字段（表里没有独立的 full_description 列）
 *   images       ← Service 层从 entity.images (JSON字符串) 解析而来
 *   mainImage    ← XML 层从 images JSON 第一张取，或 Service 层 enrichImages() 补充
 */
@Data
public class SpotDetailVO {

    private Long id;

    /** 观测点名称（← spot_name） */
    private String name;

    private String province;
    private String city;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer altitude;

    /**
     * Bortle 暗天等级 1-9（← light_pollution_level）
     * 1=最佳暗天, 9=城市天空
     */
    private Integer bortleLevel;

    private BigDecimal rating;
    private Integer ratingCount;

    /** 简介（← description） */
    private String description;

    /**
     * 完整描述（当前复用 description，表里没有独立的 full_description 列）
     * 6.5 管理员编辑时如需区分，可在数据库 ALTER TABLE 加 full_description 列
     */
    private String fullDescription;

    /**
     * 图片 URL 列表（已从 JSON 字符串解析）
     * Service 层 enrichImages() 负责填充
     */
    private List<String> images;

    /** 主图（images 第一张，XML 层直接取 JSON_EXTRACT） */
    private String mainImage;

    /** 当前登录用户评分（未评分/未登录为 null） */
    private Integer myScore;

    /** 今日签到人次 */
    private Integer todayCheckinCount;

    /** 累计签到人次 */
    private Integer totalCheckinCount;

    private LocalDateTime createTime;
}
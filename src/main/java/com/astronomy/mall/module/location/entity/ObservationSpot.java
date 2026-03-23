package com.astronomy.mall.module.location.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 天文观测点实体类
 * 对应数据库表: tb_observation_spot
 *
 * 📌 Bortle等级说明:
 *   lightPollutionLevel: 1-9，越小越暗越好
 *   1 = 最佳暗天（专业天文台级别）
 *   5 = 郊区天空（普通爱好者常用）
 *   9 = 城市天空（严重光污染，无法观星）
 *
 * 📌 坐标系: 高德GCJ-02，与高德地图JS API保持一致
 *
 * 📌 rating/rating_count 为冗余字段:
 *   每次 tb_spot_rating 有新评分时，重新计算并写入
 *   查询时无需JOIN聚合，直接读取本字段
 */
@Data
@TableName("tb_observation_spot")
public class ObservationSpot {

    /** 观测点ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 观测点名称 */
    private String spotName;

    /** 经度(高德GCJ-02坐标系) */
    private BigDecimal longitude;

    /** 纬度(高德GCJ-02坐标系) */
    private BigDecimal latitude;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 详细地址描述 */
    private String address;

    /** 海拔(米)，越高越适合观测 */
    private Integer altitude;

    /**
     * Bortle暗天等级 1-9（越小越暗越好）
     * 1=最佳暗天, 5=郊区, 9=城市
     */
    private Integer lightPollutionLevel;

    /** 综合评分(0-5星，冗余字段) */
    private BigDecimal rating;

    /** 评分人数(冗余字段) */
    private Integer ratingCount;

    /** 观测点描述（推荐设备/最佳季节等） */
    private String description;

    /** 图片URL列表（JSON数组字符串） */
    private String images;

    /** 历史总签到次数（冗余，签到时+1） */
    private Integer checkinCount;

    /** 逻辑删除(0正常 1删除) */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
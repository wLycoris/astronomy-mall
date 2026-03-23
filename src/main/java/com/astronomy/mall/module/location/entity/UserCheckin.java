package com.astronomy.mall.module.location.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户观测点签到记录实体类
 * 对应数据库表: tb_user_checkin
 *
 * 📌 防重复设计:
 *   数据库唯一约束 uk_user_spot_date (user_id, spot_id, checkin_date)
 *   每个用户每天对同一观测点只能签到一次
 *
 * 📌 快照字段:
 *   weather/moon_phase 记录签到时刻的天气和月相，用于历史足迹展示
 */
@Data
@TableName("tb_user_checkin")
public class UserCheckin {

    /** 签到ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID (关联tb_user.id) */
    private Long userId;

    /** 观测点ID (关联tb_observation_spot.id) */
    private Long spotId;

    /** 签到时用户GPS经度(GCJ-02坐标) */
    private BigDecimal longitude;

    /** 签到时用户GPS纬度(GCJ-02坐标) */
    private BigDecimal latitude;

    /** 签到时天气快照（晴/多云/阴/雨等，来自高德天气API） */
    private String weather;

    /** 签到时月相快照（新月/眉月/上弦月/盈凸月/满月/亏凸月/下弦月/残月） */
    private String moonPhase;

    /**
     * 签到日期（每日去重依据）
     * 注意: 使用 LocalDate 不含时间，防止跨时区时分秒差异导致去重失效
     */
    private LocalDate checkinDate;

    /** 签到时间（精确时刻） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
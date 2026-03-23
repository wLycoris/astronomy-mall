package com.astronomy.mall.module.location.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到历史记录VO
 * 对应接口: GET /api/location/checkin/my（我的签到足迹）
 *
 * 📌 6.0 骨架，接口逻辑在 6.3 节实现
 */
@Data
public class CheckinVO {

    /** 签到记录ID */
    private Long id;

    /** 观测点ID */
    private Long spotId;

    /** 观测点名称（JOIN获取） */
    private String spotName;

    /** 省份（JOIN获取） */
    private String province;

    /** 城市（JOIN获取） */
    private String city;

    /** 签到时天气快照 */
    private String weather;

    /** 签到时月相快照 */
    private String moonPhase;

    /** 签到日期 */
    private LocalDate checkinDate;

    /** 签到时间（精确时刻） */
    private LocalDateTime createTime;
}
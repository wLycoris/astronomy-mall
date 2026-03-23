package com.astronomy.mall.module.location.vo;

import lombok.Data;

/**
 * 今晚观测条件综合评估VO
 * 对应接口: GET /api/location/tonight
 *
 * 📌 6.0 骨架，评分算法在 6.2 节实现
 *
 * 综合评分算法（满分100分）:
 *   天气权重 50分: 晴=50, 多云=35, 阴=15, 雨/雪=0
 *   月相权重 30分: 新月=30, 眉月=25, 上弦=20, 盈凸=10, 满月=0
 *   温度权重 20分: 0-25°C=20, 超出范围按比例扣分
 *
 * 综合评分区间:
 *   80-100 = 极佳 🌟🌟🌟🌟🌟
 *   60-79  = 良好 🌟🌟🌟🌟
 *   40-59  = 一般 🌟🌟🌟
 *   20-39  = 较差 🌟🌟
 *   0-19   = 不宜 🌟
 */
@Data
public class TonightVO {

    /**
     * 综合评分（0-100）
     */
    private Integer score;

    /**
     * 评分等级文字
     * 例: "极佳" / "良好" / "一般" / "较差" / "不宜"
     */
    private String scoreLevel;

    /**
     * 月相名称
     * 例: 新月、眉月、上弦月、盈凸月、满月、亏凸月、下弦月、残月
     */
    private String moonPhaseName;

    /**
     * 月相照明百分比（0-100）
     * 0=新月（最佳），100=满月（最差）
     */
    private Integer moonIllumination;

    /**
     * 当前天气描述
     * 例: 晴、多云
     */
    private String weather;

    /**
     * 当前温度（°C）
     */
    private String temperature;

    /**
     * 综合建议文字
     * 例: "今晚极佳！月相新月，天空晴朗，强烈推荐出门观测 🔭"
     */
    private String suggestion;

    /**
     * 查询的城市/区域名称
     */
    private String cityName;
}
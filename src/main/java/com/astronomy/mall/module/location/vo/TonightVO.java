package com.astronomy.mall.module.location.vo;

import lombok.Data;

/**
 * 今晚观测条件综合评估 VO
 * 对应接口: GET /api/location/tonight
 *
 * 综合评分算法:
 *   综合评分 = 天气适宜度分 × 0.6 + 月相评分 × 0.4
 *   月相评分: 新月100分 → 满月0分（线性，illumination越低越好）
 *
 * 综合评分区间 → 星级:
 *   ≥80 = 5星极佳 / 60-79 = 4星良好 / 40-59 = 3星一般 / 20-39 = 2星较差 / <20 = 1星不宜
 *
 * ✅ 6.2 实现
 */
@Data
public class TonightVO {

    /** 天气适宜度评分（来自 getWeather 的 suitabilityScore） */
    private Integer weatherSuitability;

    /**
     * 月相周期位置（0.0~1.0）
     * 0.0=新月, 0.5=满月
     */
    private Double moonPhase;

    /**
     * 月面照明百分比（0~100）
     * 0=新月（最佳观测），100=满月（最差观测）
     */
    private Integer moonIllumination;

    /** 月相名称: 新月/峨眉月/上弦月/盈凸月/满月/亏凸月/下弦月/残月 */
    private String moonPhaseName;

    /** 综合评分（0-100） */
    private Integer overallScore;

    /** 综合星级（1-5） */
    private Integer overallStars;

    /** 综合建议文字 */
    private String suggestion;
}

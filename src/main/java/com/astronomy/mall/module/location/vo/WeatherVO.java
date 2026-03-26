package com.astronomy.mall.module.location.vo;

import lombok.Data;

/**
 * 天气数据 + 观测适宜度 VO
 * 对应接口: GET /api/location/weather
 * 数据来源: 高德天气 API（实况天气 base 类型）
 *
 * 适宜度评分算法（满分100分）:
 *   晴天+40 / 湿度<60%+20 / 湿度60-80%+10 / 湿度>80%-20
 *   风力1-3级+20 / 4-5级+0 / 6级+-20 / 温度5-25℃+20
 *   总分≥80极佳(绿) / 60-79良好(浅绿) / 40-59一般(黄) / 20-39较差(橙) / <20不宜(红)
 *
 * ✅ 6.2 实现
 */
@Data
public class WeatherVO {

    /** 城市名称（高德返回） */
    private String city;

    /** 天气状况描述，例: 晴、多云、阴、小雨 */
    private String condition;

    /** 实时温度（°C） */
    private String temperature;

    /** 空气湿度（百分比数值，例: "65"） */
    private String humidity;

    /** 风力等级，例: "3" */
    private String windLevel;

    /** 风向描述，例: 北风、东南风 */
    private String windDirection;

    /** 数据发布时间（高德返回） */
    private String reportTime;

    /** 观测适宜度评分（0-100） */
    private Integer suitabilityScore;

    /**
     * 适宜度等级文字
     * 极佳 / 良好 / 一般 / 较差 / 不宜
     */
    private String suitabilityLevel;

    /**
     * 适宜度等级对应颜色
     * #00e676(绿) / #69f0ae(浅绿) / #ffd740(黄) / #ff9100(橙) / #ff1744(红)
     */
    private String suitabilityColor;
}

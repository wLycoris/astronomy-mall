package com.astronomy.mall.module.location.vo;

import lombok.Data;

/**
 * 天气数据VO
 * 对应接口: GET /api/location/weather
 * 数据来源: 高德天气 API（weatherInfo 类型，实况天气）
 *
 * 📌 6.0 骨架，字段在 6.2 节按需调整
 * 📌 高德天气API文档: https://lbs.amap.com/api/webservice/guide/api/weatherinfo
 */
@Data
public class WeatherVO {

    /** 城市名称（高德返回） */
    private String city;

    /**
     * 天气状况描述
     * 例: 晴、多云、阴、小雨、中雨、大雨、雷阵雨
     */
    private String weather;

    /** 实时温度（°C） */
    private String temperature;

    /**
     * 风向描述
     * 例: 北风、东南风
     */
    private String windDirection;

    /**
     * 风力等级
     * 例: 3（3级风）
     */
    private String windPower;

    /** 空气湿度（百分比，例："65"） */
    private String humidity;

    /** 数据发布时间（高德返回，例："2024-08-01 18:00:00"） */
    private String reportTime;
}
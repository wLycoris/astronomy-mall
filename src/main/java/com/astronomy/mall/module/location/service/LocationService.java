package com.astronomy.mall.module.location.service;

import com.astronomy.mall.module.location.dto.SpotRatingDTO;
import com.astronomy.mall.module.location.vo.CheckinVO;
import com.astronomy.mall.module.location.vo.ObservationSpotVO;
import com.astronomy.mall.module.location.vo.SpotDetailVO;
import com.astronomy.mall.module.location.vo.WeatherVO;
import com.astronomy.mall.module.location.vo.TonightVO;

import java.util.List;
import java.util.Map;

/**
 * 地理位置服务接口
 *
 * 6.1 实现: getSpots / getSpotDetail / submitRating
 * 6.2 TODO: getWeather / getTonightCondition
 * 6.3 TODO: checkin / getCheckinHistory
 * 6.4 TODO: updateUserLocation（在 UserService 或单独接口中）
 */
public interface LocationService {

    // ==============================
    // 6.1 观测点列表、地图与评分
    // ==============================

    /**
     * 获取附近观测点（支持省市/光污染筛选，按距离升序）
     */
    List<ObservationSpotVO> getSpots(
            Double longitude, Double latitude,
            Integer radius, Integer limit,
            String province, String city,
            Integer maxLightPollution,
            Long currentUserId);

    /**
     * 获取观测点详情（含完整描述/图片列表/签到统计/当前用户评分）
     */
    SpotDetailVO getSpotDetail(Long spotId, Long currentUserId);

    /**
     * 提交评分（每人每点限1次，自动更新均值）
     * 返回: { newRating: BigDecimal, ratingCount: int }
     */
    Map<String, Object> submitRating(Long spotId, Long userId, SpotRatingDTO ratingDTO);

    // ==============================
    // 6.2 天气 + 今晚观测条件（✅ 已实现）
    // ==============================

    /**
     * 获取天气 + 观测适宜度评分
     * 后端调用高德天气API（web-key不暴露前端）
     */
    WeatherVO getWeather(Double longitude, Double latitude);

    /**
     * 今晚综合观测评估
     * 综合评分 = 天气适宜度×0.6 + 月相评分×0.4
     */
    TonightVO getTonightCondition(Double longitude, Double latitude);

    // ==============================
    // 6.3 签到与我的足迹（✅ 已实现）
    // ==============================

    /**
     * 观测点签到（距离≤5km + 每日每点去重 + 天气/月相快照 + 通知）
     * 返回: CheckinResultMap { todayCheckinCount, weather, moonPhaseName }
     */
    Map<String, Object> checkin(Long spotId, Double longitude, Double latitude, Long userId);

    /**
     * 我的签到历史（分页，含观测点名称/天气/月相快照）
     * 返回: Map { list, total, pageNum, pageSize }
     */
    Map<String, Object> getCheckinHistory(Long userId, Integer pageNum, Integer pageSize);
}
package com.astronomy.mall.module.location.service;

import com.astronomy.mall.module.location.dto.SpotRatingDTO;
import com.astronomy.mall.module.location.vo.ObservationSpotVO;
import com.astronomy.mall.module.location.vo.SpotDetailVO;

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
    // 6.2 天气评估（TODO）
    // ==============================

    Object getWeather(Double longitude, Double latitude);

    Object getTonightCondition(Double longitude, Double latitude);

    // ==============================
    // 6.3 签到（TODO）
    // ==============================

    Object checkin(Long spotId, Double longitude, Double latitude, Long userId);

    Object getCheckinHistory(Long userId, Integer pageNum, Integer pageSize);
}
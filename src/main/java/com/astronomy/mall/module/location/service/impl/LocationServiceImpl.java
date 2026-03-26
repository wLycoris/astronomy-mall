package com.astronomy.mall.module.location.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.location.dto.SpotRatingDTO;
import com.astronomy.mall.module.location.entity.ObservationSpot;
import com.astronomy.mall.module.location.entity.SpotRating;
import com.astronomy.mall.module.location.entity.UserCheckin;
import com.astronomy.mall.module.location.mapper.ObservationSpotMapper;
import com.astronomy.mall.module.location.mapper.SpotRatingMapper;
import com.astronomy.mall.module.location.mapper.UserCheckinMapper;
import com.astronomy.mall.module.location.service.LocationService;
import com.astronomy.mall.module.location.vo.CheckinVO;
import com.astronomy.mall.module.location.vo.ObservationSpotVO;
import com.astronomy.mall.module.location.vo.SpotDetailVO;
import com.astronomy.mall.module.location.vo.WeatherVO;
import com.astronomy.mall.module.location.vo.TonightVO;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地理位置服务实现
 *
 * ⚠️ 实体类字段名对照（避免再次踩坑）:
 *   entity.spotName            → DB: spot_name
 *   entity.lightPollutionLevel → DB: light_pollution_level
 *   entity.ratingCount         → DB: rating_count
 *   entity.checkinCount        → DB: checkin_count
 *   entity.deleted             → DB: deleted  （@TableLogic，值1=已删除）
 *   entity.images              → DB: images   （JSON数组字符串）
 *   entity.rating              → DB: rating
 *
 * 6.1 实现: getSpots / getSpotDetail / submitRating
 * 6.2 实现: getWeather / getTonightCondition
 * 6.3 实现: checkin / getCheckinHistory
 */
@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    /** 高德天气API web-key（后端持有，不暴露前端） */
    @Value("${amap.web-key}")
    private String amapWebKey;

    @Autowired
    private ObservationSpotMapper observationSpotMapper;

    @Autowired
    private SpotRatingMapper spotRatingMapper;

    @Autowired
    private UserCheckinMapper userCheckinMapper;

    @Autowired
    private NotificationHelper notificationHelper;

    /** 用于调用高德天气API */
    private final RestTemplate restTemplate = new RestTemplate();

    // ================================================================
    // ① 附近观测点列表（6.1）
    // ================================================================

    @Override
    public List<ObservationSpotVO> getSpots(
            Double longitude, Double latitude,
            Integer radius, Integer limit,
            String province, String city,
            Integer maxLightPollution,
            Long currentUserId) {

        if (longitude == null || latitude == null) {
            return Collections.emptyList();
        }
        if (radius == null || radius <= 0) radius = 100;
        if (radius > 500) radius = 500;
        if (limit == null || limit <= 0) limit = 20;
        if (limit > 50) limit = 50;

        return observationSpotMapper.selectNearbySpots(
                longitude, latitude, radius, limit,
                province, city, maxLightPollution, currentUserId
        );
    }

    // ================================================================
    // ② 观测点详情（6.1）
    // ================================================================

    @Override
    public SpotDetailVO getSpotDetail(Long spotId, Long currentUserId) {
        SpotDetailVO detail = observationSpotMapper.selectSpotDetail(spotId, currentUserId);
        if (detail == null) {
            throw new BusinessException("观测点不存在或已下架");
        }
        // 解析 images JSON 字符串 → List<String>，补充到 detail.images
        enrichImages(detail, spotId);
        return detail;
    }

    /**
     * 从 entity 取 images 字段（JSON字符串），解析后填入 VO
     */
    private void enrichImages(SpotDetailVO detail, Long spotId) {
        try {
            ObservationSpot spot = observationSpotMapper.selectById(spotId);
            if (spot != null && StringUtils.hasText(spot.getImages())) {
                detail.setImages(JSON.parseArray(spot.getImages(), String.class));
                // mainImage：若 XML 层没取到，用 images 第一张补
                if (!StringUtils.hasText(detail.getMainImage()) && !detail.getImages().isEmpty()) {
                    detail.setMainImage(detail.getImages().get(0));
                }
            } else {
                detail.setImages(Collections.emptyList());
            }
        } catch (Exception e) {
            log.warn("[SpotDetail] 解析图片列表失败, spotId={}", spotId, e);
            detail.setImages(Collections.emptyList());
        }
    }

    // ================================================================
    // ③ 评分提交（6.1）
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitRating(Long spotId, Long userId, SpotRatingDTO ratingDTO) {

        // 1. 查观测点是否存在
        // ⚠️ @TableLogic 注解：selectById 会自动加 WHERE deleted=0
        //    所以直接判断 null 即可，不需要再判断 spot.getDeleted()
        ObservationSpot spot = observationSpotMapper.selectById(spotId);
        if (spot == null) {
            throw new BusinessException("观测点不存在");
        }

        // 2. 检查是否已评分 → 已评过则更新，未评过则新增
        Integer existingScore = spotRatingMapper.selectUserScore(userId, spotId);
        if (existingScore != null) {
            // 已评分 → 更新评分（支持修改）
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SpotRating> uw =
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
            uw.eq("user_id", userId).eq("spot_id", spotId);
            SpotRating updateRating = new SpotRating();
            updateRating.setScore(ratingDTO.getScore());
            spotRatingMapper.update(updateRating, uw);
            log.info("[Rating] 用户修改评分: userId={}, spotId={}, {}星→{}星",
                    userId, spotId, existingScore, ratingDTO.getScore());
        } else {
            // 未评分 → 新增
            SpotRating rating = new SpotRating();
            rating.setSpotId(spotId);
            rating.setUserId(userId);
            rating.setScore(ratingDTO.getScore());
            spotRatingMapper.insert(rating);
        }

        // 4. 重新计算并更新观测点平均分+评分人数
        spotRatingMapper.updateSpotRating(spotId);

        // 5. 重新查最新数据返回前端
        // ⚠️ @TableLogic 影响：selectById 返回的是逻辑未删除的记录
        ObservationSpot updated = observationSpotMapper.selectById(spotId);

        Map<String, Object> result = new HashMap<>(4);
        result.put("newRating",   updated.getRating()      != null ? updated.getRating()      : BigDecimal.ZERO);
        result.put("ratingCount", updated.getRatingCount() != null ? updated.getRatingCount() : 0);
        return result;
    }

    // ================================================================
    // ④ 天气 + 观测适宜度（6.2 ✅）
    // ================================================================

    @Override
    public WeatherVO getWeather(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            throw new BusinessException("经纬度参数不能为空");
        }

        // 1. 经纬度 → 高德行政区编码（逆地理编码）
        String adcode = getAdcodeByLocation(longitude, latitude);

        // 2. 调用高德天气API（实况天气 base 类型）
        String weatherUrl = String.format(
                "https://restapi.amap.com/v3/weather/weatherInfo?key=%s&city=%s&extensions=base",
                amapWebKey, adcode
        );
        log.info("[Weather] 请求高德天气: adcode={}, url={}", adcode, weatherUrl);

        String body;
        try {
            body = restTemplate.getForObject(weatherUrl, String.class);
        } catch (Exception e) {
            log.error("[Weather] 高德天气API请求失败", e);
            throw new BusinessException("天气数据获取失败，请稍后重试");
        }

        JSONObject resp = JSON.parseObject(body);
        if (!"1".equals(resp.getString("status"))) {
            log.warn("[Weather] 高德天气API返回异常: {}", body);
            throw new BusinessException("天气数据获取失败: " + resp.getString("info"));
        }

        JSONArray lives = resp.getJSONArray("lives");
        if (lives == null || lives.isEmpty()) {
            throw new BusinessException("未查询到该位置天气数据");
        }

        JSONObject live = lives.getJSONObject(0);

        // 3. 解析天气数据
        WeatherVO vo = new WeatherVO();
        vo.setCity(live.getString("city"));
        vo.setCondition(live.getString("weather"));
        vo.setTemperature(live.getString("temperature"));
        vo.setHumidity(live.getString("humidity"));
        vo.setWindLevel(live.getString("windpower"));
        vo.setWindDirection(live.getString("winddirection"));
        vo.setReportTime(live.getString("reporttime"));

        // 4. 计算观测适宜度评分
        int score = calculateWeatherSuitability(
                vo.getCondition(), vo.getHumidity(), vo.getWindLevel(), vo.getTemperature()
        );
        vo.setSuitabilityScore(score);

        // 5. 评分 → 等级 + 颜色
        if (score >= 80) {
            vo.setSuitabilityLevel("极佳");
            vo.setSuitabilityColor("#00e676");
        } else if (score >= 60) {
            vo.setSuitabilityLevel("良好");
            vo.setSuitabilityColor("#69f0ae");
        } else if (score >= 40) {
            vo.setSuitabilityLevel("一般");
            vo.setSuitabilityColor("#ffd740");
        } else if (score >= 20) {
            vo.setSuitabilityLevel("较差");
            vo.setSuitabilityColor("#ff9100");
        } else {
            vo.setSuitabilityLevel("不宜");
            vo.setSuitabilityColor("#ff1744");
        }

        log.info("[Weather] 天气查询完成: city={}, condition={}, score={}, level={}",
                vo.getCity(), vo.getCondition(), score, vo.getSuitabilityLevel());
        return vo;
    }

    // ================================================================
    // ⑤ 今晚综合观测评估（6.2 ✅）
    // ================================================================

    @Override
    public TonightVO getTonightCondition(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            throw new BusinessException("经纬度参数不能为空");
        }

        // 1. 复用 getWeather() 获取天气适宜度分
        WeatherVO weather = getWeather(longitude, latitude);

        // 2. 计算今晚月相
        LocalDate today = LocalDate.now();
        double phase = calculateMoonPhaseValue(today);
        double illumination = (1 - Math.cos(phase * 2 * Math.PI)) / 2 * 100;
        illumination = Math.round(illumination * 10.0) / 10.0;
        String phaseName = getMoonPhaseName(phase);

        // 3. 月相评分: 新月100分 → 满月0分（线性，illumination越低越好）
        int moonScore = (int) Math.round(100 - illumination);

        // 4. 综合评分 = 天气分×0.6 + 月相分×0.4
        int overallScore = (int) Math.round(weather.getSuitabilityScore() * 0.6 + moonScore * 0.4);

        // 5. 综合评分 → 星级
        int stars;
        if (overallScore >= 80) stars = 5;
        else if (overallScore >= 60) stars = 4;
        else if (overallScore >= 40) stars = 3;
        else if (overallScore >= 20) stars = 2;
        else stars = 1;

        // 6. 生成建议文字
        String suggestion = generateSuggestion(stars, phaseName, weather.getCondition());

        // 7. 组装结果
        TonightVO vo = new TonightVO();
        vo.setWeatherSuitability(weather.getSuitabilityScore());
        vo.setMoonPhase(Math.round(phase * 1000.0) / 1000.0);
        vo.setMoonIllumination((int) Math.round(illumination));
        vo.setMoonPhaseName(phaseName);
        vo.setOverallScore(overallScore);
        vo.setOverallStars(stars);
        vo.setSuggestion(suggestion);

        log.info("[Tonight] 今晚评估完成: weather={}, moon={}({}%), overall={}, stars={}",
                weather.getSuitabilityScore(), phaseName, (int) illumination, overallScore, stars);
        return vo;
    }

    // ================================================================
    // ⑥ 观测点签到（6.3 ✅）
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkin(Long spotId, Double longitude, Double latitude, Long userId) {
        // 1. 查询观测点（@TableLogic 自动过滤 deleted=1）
        ObservationSpot spot = observationSpotMapper.selectById(spotId);
        if (spot == null) {
            throw new BusinessException("观测点不存在或已下架");
        }

        // 2. 距离校验：Haversine 计算用户与观测点距离，>5km 拒绝签到
        if (longitude != null && latitude != null) {
            double dist = haversineKm(
                    latitude, longitude,
                    spot.getLatitude().doubleValue(), spot.getLongitude().doubleValue()
            );
            if (dist > 5.0) {
                throw new BusinessException(String.format("距离观测点 %.1f km，需在 5km 内才能签到", dist));
            }
        }

        // 3. 每日每点去重：查唯一键 uk_user_spot_date
        LocalDate today = LocalDate.now();
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCheckin> qw =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("user_id", userId).eq("spot_id", spotId).eq("checkin_date", today);
        Long existCount = userCheckinMapper.selectCount(qw);
        if (existCount != null && existCount > 0) {
            throw new BusinessException("今日已在该观测点签到，明天再来吧");
        }

        // 4. 获取天气/月相快照（签到时记录，用于足迹展示）
        String weatherSnapshot = "";
        String moonPhaseSnapshot = "";
        try {
            if (longitude != null && latitude != null) {
                WeatherVO weather = getWeather(longitude, latitude);
                weatherSnapshot = weather.getCondition();
            }
        } catch (Exception e) {
            log.warn("[Checkin] 获取天气快照失败，不影响签到: {}", e.getMessage());
        }
        try {
            double phase = calculateMoonPhaseValue(today);
            moonPhaseSnapshot = getMoonPhaseName(phase);
        } catch (Exception e) {
            log.warn("[Checkin] 获取月相快照失败: {}", e.getMessage());
        }

        // 5. 插入签到记录
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setSpotId(spotId);
        checkin.setLongitude(longitude != null ? BigDecimal.valueOf(longitude) : null);
        checkin.setLatitude(latitude != null ? BigDecimal.valueOf(latitude) : null);
        checkin.setWeather(weatherSnapshot);
        checkin.setMoonPhase(moonPhaseSnapshot);
        checkin.setCheckinDate(today);
        userCheckinMapper.insert(checkin);

        // 6. 更新观测点签到计数 +1
        ObservationSpot update = new ObservationSpot();
        update.setId(spotId);
        update.setCheckinCount(spot.getCheckinCount() != null ? spot.getCheckinCount() + 1 : 1);
        observationSpotMapper.updateById(update);

        // 7. 查今日该观测点签到总人数
        int todayCount = userCheckinMapper.countTodayCheckin(spotId, today);

        // 8. 异步发签到通知（不影响主流程）
        try {
            notificationHelper.sendCheckinNotification(userId, spot.getSpotName(), spotId, todayCount);
        } catch (Exception e) {
            log.warn("[Checkin] 发送签到通知失败: {}", e.getMessage());
        }

        // 9. 返回结果
        Map<String, Object> result = new HashMap<>(4);
        result.put("todayCheckinCount", todayCount);
        result.put("weather", weatherSnapshot);
        result.put("moonPhaseName", moonPhaseSnapshot);

        log.info("[Checkin] 签到成功: userId={}, spotId={}, spotName={}, todayCount={}",
                userId, spotId, spot.getSpotName(), todayCount);
        return result;
    }

    // ================================================================
    // ⑦ 我的签到历史（6.3 ✅）
    // ================================================================

    @Override
    public Map<String, Object> getCheckinHistory(Long userId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        if (pageSize > 50) pageSize = 50;

        int offset = (pageNum - 1) * pageSize;
        List<CheckinVO> list = userCheckinMapper.listMyCheckins(userId, offset, pageSize);
        int total = userCheckinMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>(4);
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * Haversine 公式计算两点距离（km）
     */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371.0; // 地球半径 km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ================================================================
    // 私有工具方法
    // ================================================================

    /**
     * 经纬度 → 高德行政区编码（逆地理编码）
     * 天气API需要 adcode 而非经纬度，所以先做一次逆地理编码
     */
    private String getAdcodeByLocation(Double longitude, Double latitude) {
        String regeoUrl = String.format(
                "https://restapi.amap.com/v3/geocode/regeo?key=%s&location=%s,%s",
                amapWebKey, longitude, latitude
        );
        try {
            String body = restTemplate.getForObject(regeoUrl, String.class);
            JSONObject resp = JSON.parseObject(body);
            if ("1".equals(resp.getString("status"))) {
                JSONObject regeocode = resp.getJSONObject("regeocode");
                if (regeocode != null) {
                    JSONObject addressComponent = regeocode.getJSONObject("addressComponent");
                    if (addressComponent != null) {
                        String adcode = addressComponent.getString("adcode");
                        if (StringUtils.hasText(adcode)) {
                            return adcode;
                        }
                    }
                }
            }
            log.warn("[Weather] 逆地理编码未返回adcode: {}", body);
        } catch (Exception e) {
            log.error("[Weather] 逆地理编码请求失败", e);
        }
        // 兜底: 直接用经纬度拼6位数字（高德也支持citycode形式查询）
        throw new BusinessException("无法确定该位置所属城市，请尝试切换位置");
    }

    /**
     * 天气适宜度评分算法
     * 晴天+40 / 湿度<60%+20, 60-80%+10, >80%-20
     * 风力1-3级+20, 4-5级+0, 6级以上-20 / 温度5-25℃+20
     */
    private int calculateWeatherSuitability(String condition, String humidity, String windLevel, String temperature) {
        int score = 0;

        // 天气状况评分 (+40 ~ -20)
        if (condition != null) {
            if (condition.contains("晴")) {
                score += 40;
            } else if (condition.contains("多云")) {
                score += 25;
            } else if (condition.contains("阴")) {
                score += 10;
            } else {
                // 雨/雪/雾/霾等恶劣天气
                score -= 20;
            }
        }

        // 湿度评分
        try {
            int hum = Integer.parseInt(humidity);
            if (hum < 60) {
                score += 20;
            } else if (hum <= 80) {
                score += 10;
            } else {
                score -= 20;
            }
        } catch (Exception e) {
            // 湿度解析失败不扣分
        }

        // 风力评分（高德返回可能是 "≤3" "4" "5" 等格式）
        try {
            String wl = windLevel.replaceAll("[^0-9]", "");
            if (StringUtils.hasText(wl)) {
                int wind = Integer.parseInt(wl);
                if (wind <= 3) {
                    score += 20;
                } else if (wind <= 5) {
                    // 4-5级不加不减
                } else {
                    score -= 20;
                }
            } else {
                // "≤3" 情况，取3
                score += 20;
            }
        } catch (Exception e) {
            // 风力解析失败不扣分
        }

        // 温度评分
        try {
            int temp = Integer.parseInt(temperature);
            if (temp >= 5 && temp <= 25) {
                score += 20;
            }
            // 温度不在5-25℃区间时不加分也不扣分
        } catch (Exception e) {
            // 温度解析失败不扣分
        }

        // 分数限制在 0~100
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 计算月相周期位置 0.0~1.0
     * 基准: 2000-01-06 为已知新月
     */
    private double calculateMoonPhaseValue(LocalDate date) {
        LocalDate base = LocalDate.of(2000, 1, 6);
        long days = ChronoUnit.DAYS.between(base, date);
        double synodic = 29.53059;
        double phase = ((days % synodic) + synodic) % synodic / synodic;
        return phase;
    }

    /**
     * 计算月相照明度 0~100%（兼容旧方法签名）
     */
    public double calculateMoonPhase(LocalDate date) {
        double phase = calculateMoonPhaseValue(date);
        double illumination = (1 - Math.cos(phase * 2 * Math.PI)) / 2 * 100;
        return Math.round(illumination * 10.0) / 10.0;
    }

    /**
     * 月相周期位置 → 月相中文名称
     * 新月(0) → 峨眉月 → 上弦月 → 盈凸月 → 满月(0.5) → 亏凸月 → 下弦月 → 残月
     */
    private String getMoonPhaseName(double phase) {
        if (phase < 0.0625)  return "新月";
        if (phase < 0.1875)  return "峨眉月";
        if (phase < 0.3125)  return "上弦月";
        if (phase < 0.4375)  return "盈凸月";
        if (phase < 0.5625)  return "满月";
        if (phase < 0.6875)  return "亏凸月";
        if (phase < 0.8125)  return "下弦月";
        if (phase < 0.9375)  return "残月";
        return "新月";
    }

    /**
     * 根据星级生成观测建议文字
     */
    private String generateSuggestion(int stars, String moonPhaseName, String weatherCondition) {
        switch (stars) {
            case 5:
                return "今晚观测条件极佳！" + moonPhaseName + "配合" + weatherCondition
                        + "天气，强烈推荐出门观星";
            case 4:
                return "今晚观测条件良好，" + moonPhaseName + "，" + weatherCondition
                        + "天气，适合观测亮星和星座";
            case 3:
                return "今晚观测条件一般，" + moonPhaseName + "有一定月光干扰，"
                        + "建议选择光污染较低的区域";
            case 2:
                return "今晚观测条件较差，" + moonPhaseName + "月光较强或天气不佳，"
                        + "仅适合观测月球和明亮行星";
            default:
                return "今晚不太适合天文观测，建议关注天气变化，"
                        + "等待更好的观测窗口";
        }
    }
}
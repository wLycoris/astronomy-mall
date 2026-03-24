package com.astronomy.mall.module.location.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.location.dto.SpotRatingDTO;
import com.astronomy.mall.module.location.entity.ObservationSpot;
import com.astronomy.mall.module.location.entity.SpotRating;
import com.astronomy.mall.module.location.mapper.ObservationSpotMapper;
import com.astronomy.mall.module.location.mapper.SpotRatingMapper;
import com.astronomy.mall.module.location.service.LocationService;
import com.astronomy.mall.module.location.vo.ObservationSpotVO;
import com.astronomy.mall.module.location.vo.SpotDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
 * 6.2 TODO: getWeather / getTonightCondition
 * 6.3 TODO: checkin / getCheckinHistory
 */
@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private ObservationSpotMapper observationSpotMapper;

    @Autowired
    private SpotRatingMapper spotRatingMapper;

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

        // 2. 检查是否已评分
        Integer existingScore = spotRatingMapper.selectUserScore(userId, spotId);
        if (existingScore != null) {
            throw new BusinessException("您已对该观测点评过分（" + existingScore + "星），每人每点只能评一次");
        }

        // 3. 插入评分记录
        SpotRating rating = new SpotRating();
        rating.setSpotId(spotId);
        rating.setUserId(userId);
        rating.setScore(ratingDTO.getScore());
        spotRatingMapper.insert(rating);

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
    // TODO 6.2 / 6.3 占位
    // ================================================================

    @Override
    public Object getWeather(Double longitude, Double latitude) {
        throw new BusinessException("天气功能将在 6.2 节实现");
    }

    @Override
    public Object getTonightCondition(Double longitude, Double latitude) {
        throw new BusinessException("今晚评估功能将在 6.2 节实现");
    }

    @Override
    public Object checkin(Long spotId, Double longitude, Double latitude, Long userId) {
        throw new BusinessException("签到功能将在 6.3 节实现");
    }

    @Override
    public Object getCheckinHistory(Long userId, Integer pageNum, Integer pageSize) {
        throw new BusinessException("签到历史功能将在 6.3 节实现");
    }

    // ================================================================
    // 月相算法（6.2 用到，提前实现）
    // ================================================================

    /**
     * 计算月相照明度 0~100%（0=新月最佳观测，100=满月最差观测）
     */
    public double calculateMoonPhase(LocalDate date) {
        LocalDate base = LocalDate.of(2000, 1, 6); // 已知新月基准
        long days = ChronoUnit.DAYS.between(base, date);
        double phase = (days % 29.53059) / 29.53059;
        double illumination = (1 - Math.cos(phase * 2 * Math.PI)) / 2 * 100;
        return Math.round(illumination * 10.0) / 10.0;
    }
}
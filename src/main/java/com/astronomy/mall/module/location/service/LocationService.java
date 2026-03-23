package com.astronomy.mall.module.location.service;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.location.dto.CheckinDTO;
import com.astronomy.mall.module.location.dto.SpotRatingDTO;
import com.astronomy.mall.module.location.vo.*;

import java.util.List;

/**
 * 地理位置服务接口（用户端）
 *
 * 接口总览（8个，6.1-6.4实现）:
 *
 * 6.1 观测点列表/地图/筛选/评分:
 *   - listSpots()        GET /api/location/spots         观测点列表（筛选）
 *   - getSpotDetail()    GET /api/location/spot/{id}     观测点详情
 *   - rateSpot()         POST /api/location/spot/{id}/rating  评分（防重复）
 *
 * 6.2 天气+今晚观测条件:
 *   - getWeather()       GET /api/location/weather       实况天气
 *   - getTonight()       GET /api/location/tonight       今晚综合评分
 *
 * 6.3 用户签到+我的足迹:
 *   - checkin()          POST /api/location/checkin      签到（防重复）
 *   - listMyCheckins()   GET /api/location/checkin/my    我的签到历史
 *
 * 6.4 地址联动:
 *   - updateUserLocation() PUT /api/user/location        更新用户常用坐标
 *
 * 📌 6.0 骨架接口，各节对应实现方法体
 */
public interface LocationService {

    // ==================== 6.1 观测点 ====================

    /**
     * 获取观测点列表（支持按省/市/光污染等级筛选）
     * 白名单接口，无需登录
     *
     * @param province          省份（可为null）
     * @param city              城市（可为null）
     * @param maxLightPollution 最大光污染Bortle等级（可为null，默认不限）
     * @return 观测点VO列表
     *
     * TODO 6.1: 实现此方法
     */
    List<ObservationSpotVO> listSpots(String province, String city, Integer maxLightPollution);

    /**
     * 获取观测点详情（含当前用户评分状态和今日签到状态）
     * 白名单接口，无需登录；userId 为 null 时 myScore/todayCheckedIn 均返回 null/false
     *
     * @param spotId 观测点ID
     * @param userId 当前登录用户ID（可为null）
     * @return 观测点详情VO
     *
     * TODO 6.1: 实现此方法
     */
    SpotDetailVO getSpotDetail(Long spotId, Long userId);

    /**
     * 用户对观测点评分（1-5星，每人每点只能评一次）
     * 需要登录
     *
     * @param spotId 观测点ID
     * @param userId 当前登录用户ID
     * @param dto    评分数据
     *
     * TODO 6.1: 实现此方法（评分后重新计算观测点均分+人数）
     */
    void rateSpot(Long spotId, Long userId, SpotRatingDTO dto);

    // ==================== 6.2 天气+今晚观测条件 ====================

    /**
     * 获取指定城市实况天气
     * 白名单接口，调用高德天气API（后端代理，不暴露Key给前端）
     *
     * @param city 城市名（例："北京"）或高德 adcode（例："110000"）
     * @return 天气VO
     *
     * TODO 6.2: 实现此方法（调用高德 weather API，做简单内存缓存）
     */
    WeatherVO getWeather(String city);

    /**
     * 获取今晚观测条件综合评分
     * 白名单接口，结合天气+月相计算综合分
     *
     * @param city 城市名或adcode
     * @return 今晚观测条件VO
     *
     * TODO 6.2: 实现此方法（天气50分+月相30分+温度20分）
     */
    TonightVO getTonight(String city);

    // ==================== 6.3 用户签到 ====================

    /**
     * 用户签到
     * 需要登录；同一用户同一观测点当天只能签到一次
     *
     * @param userId 当前登录用户ID
     * @param dto    签到数据
     * @return 签到结果VO（含签到成功提示和今日总签到人数）
     *
     * TODO 6.3: 实现此方法（插入签到记录 + 更新checkin_count + 发签到通知）
     */
    CheckinVO checkin(Long userId, CheckinDTO dto);

    /**
     * 获取用户签到历史（我的足迹，分页）
     * 需要登录
     *
     * @param userId   当前登录用户ID
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页数量
     * @return 签到记录VO列表
     *
     * TODO 6.3: 实现此方法
     */
    List<CheckinVO> listMyCheckins(Long userId, int pageNum, int pageSize);

    // ==================== 6.4 地址联动 ====================

    /**
     * 更新用户常用位置坐标
     * 在 UserAddress.vue 定位自动填充时调用
     * 写入 tb_user.longitude / latitude，供推荐系统使用
     *
     * @param userId    当前登录用户ID
     * @param longitude 经度（高德GCJ-02）
     * @param latitude  纬度（高德GCJ-02）
     *
     * TODO 6.4: 实现此方法（直接 UPDATE tb_user，注意并发安全用乐观锁或幂等写入）
     */
    void updateUserLocation(Long userId, Double longitude, Double latitude);
}
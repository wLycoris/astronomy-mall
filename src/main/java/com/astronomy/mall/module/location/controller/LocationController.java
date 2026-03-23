package com.astronomy.mall.module.location.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.location.dto.CheckinDTO;
import com.astronomy.mall.module.location.dto.SpotRatingDTO;
import com.astronomy.mall.module.location.service.LocationService;
import com.astronomy.mall.module.location.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 地理位置模块 - 用户端 Controller
 * Base URL: /api/location
 *
 * 接口清单（8个）:
 *
 * 白名单（无需登录，WebMvcConfig已配置）:
 *   GET  /api/location/spots         6.1 观测点列表+筛选
 *   GET  /api/location/spot/{id}     6.1 观测点详情
 *   GET  /api/location/weather       6.2 实况天气
 *   GET  /api/location/tonight       6.2 今晚观测条件综合评分
 *
 * 需要登录:
 *   POST /api/location/spot/{id}/rating  6.1 提交评分（防重复）
 *   POST /api/location/checkin           6.3 签到
 *   GET  /api/location/checkin/my        6.3 我的签到足迹
 *   PUT  /api/user/location              6.4 更新用户常用坐标（定义在UserController中）
 *
 * 📌 6.0 骨架：方法体仅写结构，业务逻辑在 6.1~6.3 各节填充
 */
@Slf4j
@Api(tags = "地理位置模块")
@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    // ==================== 6.1 观测点（白名单接口）====================

    /**
     * 获取观测点列表
     * 白名单，无需登录
     * GET /api/location/spots?province=&city=&maxLightPollution=
     *
     * TODO 6.1: 填充实现
     */
    @ApiOperation("获取观测点列表（支持省/市/光污染等级筛选）")
    @GetMapping("/spots")
    public Result<List<ObservationSpotVO>> listSpots(
            @ApiParam("省份，可选") @RequestParam(required = false) String province,
            @ApiParam("城市，可选") @RequestParam(required = false) String city,
            @ApiParam("最大光污染Bortle等级(1-9)，越小越好，可选") @RequestParam(required = false) Integer maxLightPollution) {
        List<ObservationSpotVO> list = locationService.listSpots(province, city, maxLightPollution);
        return Result.success(list);
    }

    /**
     * 获取观测点详情
     * 白名单，无需登录；有Token时也可识别当前用户评分/签到状态
     * GET /api/location/spot/{id}
     *
     * TODO 6.1: 填充实现
     */
    @ApiOperation("获取观测点详情（含当前用户评分和签到状态）")
    @GetMapping("/spot/{id}")
    public Result<SpotDetailVO> getSpotDetail(
            @ApiParam("观测点ID") @PathVariable Long id,
            HttpServletRequest request) {
        // 尝试获取当前用户（未登录则为null，白名单接口userId可为null）
        Long userId = (Long) request.getAttribute("userId");
        SpotDetailVO vo = locationService.getSpotDetail(id, userId);
        return Result.success(vo);
    }

    /**
     * 提交观测点评分（需要登录）
     * POST /api/location/spot/{id}/rating
     * Body: { "score": 5 }
     *
     * TODO 6.1: 填充实现
     */
    @ApiOperation("提交观测点评分（1-5星，每人每观测点只能评一次）")
    @PostMapping("/spot/{id}/rating")
    public Result<Void> rateSpot(
            @ApiParam("观测点ID") @PathVariable Long id,
            @RequestBody @Validated SpotRatingDTO dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        locationService.rateSpot(id, userId, dto);
        return Result.success();
    }

    // ==================== 6.2 天气+今晚观测条件（白名单接口）====================

    /**
     * 获取实况天气（高德API代理，Key不暴露前端）
     * 白名单，无需登录
     * GET /api/location/weather?city=北京
     *
     * TODO 6.2: 填充实现
     */
    @ApiOperation("获取实况天气（高德天气API代理，Key后端保管）")
    @GetMapping("/weather")
    public Result<WeatherVO> getWeather(
            @ApiParam("城市名或高德adcode，例：北京 或 110000") @RequestParam String city) {
        WeatherVO vo = locationService.getWeather(city);
        return Result.success(vo);
    }

    /**
     * 获取今晚观测条件综合评分
     * 白名单，无需登录
     * GET /api/location/tonight?city=北京
     *
     * TODO 6.2: 填充实现
     */
    @ApiOperation("获取今晚观测综合评分（天气+月相+温度综合计算）")
    @GetMapping("/tonight")
    public Result<TonightVO> getTonight(
            @ApiParam("城市名或高德adcode") @RequestParam String city) {
        TonightVO vo = locationService.getTonight(city);
        return Result.success(vo);
    }

    // ==================== 6.3 用户签到（需要登录）====================

    /**
     * 用户签到
     * 需要登录，每日同一观测点只能签到一次
     * POST /api/location/checkin
     * Body: { "spotId": 1, "longitude": 116.4, "latitude": 39.9 }
     *
     * TODO 6.3: 填充实现
     */
    @ApiOperation("用户观测点签到（每日每观测点限一次）")
    @PostMapping("/checkin")
    public Result<CheckinVO> checkin(
            @RequestBody @Validated CheckinDTO dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CheckinVO vo = locationService.checkin(userId, dto);
        return Result.success(vo);
    }

    /**
     * 我的签到足迹（分页）
     * 需要登录
     * GET /api/location/checkin/my?pageNum=1&pageSize=10
     *
     * TODO 6.3: 填充实现
     */
    @ApiOperation("我的签到足迹（分页，按时间倒序）")
    @GetMapping("/checkin/my")
    public Result<List<CheckinVO>> listMyCheckins(
            @ApiParam("页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CheckinVO> list = locationService.listMyCheckins(userId, pageNum, pageSize);
        return Result.success(list);
    }
}
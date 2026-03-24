package com.astronomy.mall.module.location.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.location.dto.SpotRatingDTO;
import com.astronomy.mall.module.location.service.LocationService;
import com.astronomy.mall.module.location.vo.ObservationSpotVO;
import com.astronomy.mall.module.location.vo.SpotDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 地理位置 Controller
 *
 * 接口前缀: /api/location
 *
 * ✅ 6.1 已实现 (3个):
 *   GET  /spots           - 附近观测点列表（公开，无需登录）
 *   GET  /spot/{id}       - 观测点详情（公开，无需登录）
 *   POST /spot/{id}/rating - 评分（需登录）
 *
 * TODO 6.2 (2个):
 *   GET  /weather         - 天气+适宜度
 *   GET  /tonight         - 今晚综合评估
 *
 * TODO 6.3 (2个):
 *   POST /checkin         - 签到
 *   GET  /checkin/my      - 我的签到历史
 *
 * ⚠️ JWT 白名单（WebMvcConfig.java 已配置）:
 *   /api/location/spots   ← 已放行（公开）
 *   /api/location/spot/   ← 前缀已放行（公开，含 spot/{id} 和 spot/{id}/rating 会被匹配？）
 *
 * ⚠️ 注意: /spot/{id}/rating 需要登录！
 *   WebMvcConfig 中白名单写的是 "/api/location/spot/" 前缀匹配，
 *   实际 Spring excludePathPatterns 不支持通配后缀，
 *   请确认 WebMvcConfig 里写的是:
 *     "/api/location/spot/**"  ← 会把 rating 也放行（不需要登录）
 *   还是:
 *     "/api/location/spot/{id}" ← 只放行详情
 *
 *   ★ 推荐做法：rating接口需要登录，Controller里用 request.getAttribute("userId") 取用户ID
 *     若取不到（null）说明未登录，返回401。
 *     白名单只写 /api/location/spots 和 /api/location/spot/* （两种写法对应不同精度）
 */
@Slf4j
@RestController
@RequestMapping("/api/location")
@Api(tags = "地理位置 - 观测点")
public class LocationController {

    @Autowired
    private LocationService locationService;

    // ================================================================
    // ① GET /location/spots - 附近观测点列表（公开，无需登录）
    // ================================================================

    @GetMapping("/spots")
    @ApiOperation("获取附近观测点（按距离排序，支持省市/光污染筛选）")
    public Result<List<ObservationSpotVO>> getNearbySpots(
            @ApiParam("经度（定位获取或城市中心）") @RequestParam(required = false) Double longitude,
            @ApiParam("纬度（定位获取或城市中心）") @RequestParam(required = false) Double latitude,
            @ApiParam("搜索半径(km，默认100，最大500)") @RequestParam(defaultValue = "100") Integer radius,
            @ApiParam("返回条数（默认20，最大50）") @RequestParam(defaultValue = "20") Integer limit,
            @ApiParam("省份筛选（可选）") @RequestParam(required = false) String province,
            @ApiParam("城市筛选（可选）") @RequestParam(required = false) String city,
            @ApiParam("Bortle等级上限(1-9，可选，传3=只看≤3级暗天)") @RequestParam(required = false) Integer maxLightPollution,
            HttpServletRequest request) {

        // 当前用户（可能为null，未登录时不影响列表，只影响 myScore 字段）
        Long currentUserId = (Long) request.getAttribute("userId");

        List<ObservationSpotVO> spots = locationService.getSpots(
                longitude, latitude, radius, limit,
                province, city, maxLightPollution, currentUserId
        );

        return Result.success(spots);
    }

    // ================================================================
    // ② GET /location/spot/{id} - 观测点详情（公开，无需登录）
    // ================================================================

    @GetMapping("/spot/{id}")
    @ApiOperation("获取观测点详情（含完整描述/图片/签到统计）")
    public Result<SpotDetailVO> getSpotDetail(
            @ApiParam("观测点ID") @PathVariable Long id,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        SpotDetailVO detail = locationService.getSpotDetail(id, currentUserId);
        return Result.success(detail);
    }

    // ================================================================
    // ③ POST /location/spot/{id}/rating - 提交评分（需登录）
    // ================================================================

    @PostMapping("/spot/{id}/rating")
    @ApiOperation("提交观测点评分（1-5星，每人每点限1次）")
    public Result<Map<String, Object>> submitRating(
            @ApiParam("观测点ID") @PathVariable Long id,
            @Validated @RequestBody SpotRatingDTO ratingDTO,
            HttpServletRequest request) {

        // 评分接口必须登录
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录后再评分");
        }

        Map<String, Object> result = locationService.submitRating(id, userId, ratingDTO);
        return Result.success(result);
    }

    // ================================================================
    // TODO 6.2: 天气接口（占位，返回提示信息）
    // ================================================================

    @GetMapping("/weather")
    @ApiOperation("TODO 6.2: 天气+观测适宜度（待开发）")
    public Result<Object> getWeather(
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude) {
        // TODO 6.2 实现：调用高德天气 API（web-key 后端持有，不暴露前端）
        return Result.error("天气功能将在 6.2 节实现");
    }

    @GetMapping("/tonight")
    @ApiOperation("TODO 6.2: 今晚综合观测评估（待开发）")
    public Result<Object> getTonightCondition(
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude) {
        // TODO 6.2 实现：天气×0.6 + 月相×0.4 综合评分
        return Result.error("今晚评估功能将在 6.2 节实现");
    }

    // ================================================================
    // TODO 6.3: 签到接口（占位，返回提示信息）
    // ================================================================

    @PostMapping("/checkin")
    @ApiOperation("TODO 6.3: 观测点签到（待开发）")
    public Result<Object> checkin(HttpServletRequest request) {
        // TODO 6.3 实现：距离≤5km + 每日每点去重 + 发签到通知
        return Result.error("签到功能将在 6.3 节实现");
    }

    @GetMapping("/checkin/my")
    @ApiOperation("TODO 6.3: 我的签到历史（待开发）")
    public Result<Object> getCheckinHistory(HttpServletRequest request) {
        // TODO 6.3 实现：分页查询 tb_user_checkin，含天气/月相快照
        return Result.error("签到历史功能将在 6.3 节实现");
    }
}
package com.astronomy.mall.module.nasa.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.nasa.service.NasaApiService;
import com.astronomy.mall.module.nasa.vo.ApodVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NASA API 控制器
 *
 * 📌 接口列表：
 * - GET /api/nasa/apod   获取今日 APOD（公开，无需登录）
 *
 * 📌 白名单说明：
 * /api/nasa/** 已在 WebMvcConfig JwtInterceptor excludePathPatterns 中排除
 * 前端商城首页 ApodCard.vue 无需 Token 即可调用
 *
 * 📌 缓存说明：
 * NasaApiService.getTodayApod() 内部有当日内存缓存，Controller 无需额外处理
 */
@Slf4j
@RestController
@RequestMapping("/api/nasa")
@Api(tags = "NASA API")
public class NasaController {

    @Autowired
    private NasaApiService nasaApiService;

    /**
     * 获取今日 NASA 每日天文图片 (APOD)
     *
     * 📌 公开接口，无需登录
     * 📌 当日内存缓存：同一天内反复调用只请求 NASA API 一次
     * 📌 失败处理：NASA API 调用失败时返回 data=null，前端静默隐藏 ApodCard
     *
     * @return ApodVO（date/title/explanation/url/hdurl/mediaType/copyright）
     */
    @GetMapping("/apod")
    @ApiOperation("获取今日NASA天文图片（APOD）")
    public Result<ApodVO> getTodayApod() {
        log.debug("[NasaController] 收到 APOD 请求");
        ApodVO apod = nasaApiService.getTodayApod();
        if (apod == null) {
            // NASA API 调用失败，返回成功但 data 为 null
            // 前端 ApodCard.vue 捕获后设置 loadFailed=true，静默隐藏组件
            log.warn("[NasaController] APOD 返回为 null，NASA API 可能暂时不可用");
            return Result.success(null);
        }
        return Result.success(apod);
    }
}
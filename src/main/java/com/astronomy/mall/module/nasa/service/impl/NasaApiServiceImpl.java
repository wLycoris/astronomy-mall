package com.astronomy.mall.module.nasa.service.impl;

import com.astronomy.mall.module.nasa.service.NasaApiService;
import com.astronomy.mall.module.nasa.vo.ApodVO;
import com.astronomy.mall.module.nasa.vo.MarsPhotoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NASA API 服务实现类
 *
 * 📌 APOD 当日内存缓存：同一天重复调用直接返回缓存
 * 📌 Mars Rover 降级策略：Perseverance → Curiosity
 * 📌 NASA API 官方限额每小时 1000 次，缓存保证全天只调 1 次
 *
 * @see NasaApiService
 */
@Slf4j
@Service
public class NasaApiServiceImpl implements NasaApiService {

    // ============================================================
    // 常量：NASA API 基础 URL
    // ============================================================

    /** NASA APOD API 地址 */
    private static final String APOD_URL =
            "https://api.nasa.gov/planetary/apod?api_key=";

    /** Perseverance 最新照片 API（优先） */
    private static final String PERSEVERANCE_URL =
            "https://api.nasa.gov/mars-photos/api/v1/rovers/perseverance/latest_photos?api_key=";

    /** Curiosity 最新照片 API（降级备用） */
    private static final String CURIOSITY_URL =
            "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/latest_photos?api_key=";

    // ============================================================
    // 依赖注入
    // ============================================================

    /** Spring 内置 RestTemplate，在 NasaConfig 中注册为 Bean */
    @Autowired
    private RestTemplate restTemplate;

    /** NASA API Key，从 application.yml 读取 */
    @Value("${nasa.api-key}")
    private String nasaApiKey;

    // ============================================================
    // APOD 当日内存缓存（非持久化，重启后重新请求）
    // ============================================================

    /** 当日 APOD 缓存结果 */
    private ApodVO todayApodCache = null;

    /** 缓存对应的日期，与当日比对决定是否命中 */
    private LocalDate apodCacheDate = null;

    // ============================================================
    // 接口实现
    // ============================================================

    /**
     * 获取今日 APOD（带当日内存缓存 + synchronized 线程安全）
     *
     * 📌 流程：
     * 1. 检查缓存日期是否等于今日 → 命中直接返回
     * 2. 未命中 → 调用 NASA APOD API → 组装 ApodVO → 存入缓存
     *
     * @return ApodVO，调用方可直接使用
     */
    @Override
    public synchronized ApodVO getTodayApod() {
        LocalDate today = LocalDate.now();

        // 命中当日缓存，直接返回
        if (todayApodCache != null && today.equals(apodCacheDate)) {
            log.debug("[NasaApiService] APOD 命中当日缓存，日期: {}", today);
            return todayApodCache;
        }

        log.info("[NasaApiService] 调用 NASA APOD API，日期: {}", today);
        try {
            String url = APOD_URL + nasaApiKey;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) {
                log.warn("[NasaApiService] APOD API 返回 body 为 null");
                return null;
            }

            // 组装 VO
            ApodVO vo = new ApodVO();
            vo.setDate((String) body.get("date"));
            vo.setTitle((String) body.get("title"));
            vo.setExplanation((String) body.get("explanation"));
            vo.setUrl((String) body.get("url"));
            vo.setHdurl((String) body.get("hdurl"));
            // NASA 返回字段名是 media_type，Java 对象用 camelCase
            vo.setMediaType((String) body.get("media_type"));
            vo.setCopyright((String) body.get("copyright"));

            // 存入缓存
            todayApodCache = vo;
            apodCacheDate = today;

            log.info("[NasaApiService] APOD 获取成功，标题: {}", vo.getTitle());
            return vo;

        } catch (Exception e) {
            log.error("[NasaApiService] 调用 NASA APOD API 失败: {}", e.getMessage(), e);
            // 失败返回 null，由调用方（Controller / Scheduler）处理
            return null;
        }
    }

    /**
     * 获取火星车最新照片，取前3张
     *
     * 📌 降级策略：
     * 1. 先请求 Perseverance（好奇号兄弟，2021年登陆）
     * 2. 若 latest_photos 为空列表，切换 Curiosity（2012年登陆，更多历史数据）
     * 3. 仍为空则返回空列表，Scheduler 跳过本次同步
     *
     * @return 最多3张照片，可能为空列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<MarsPhotoVO> getLatestMarsPhotos() {
        log.info("[NasaApiService] 开始获取火星车最新照片...");

        // 1. 尝试 Perseverance
        List<MarsPhotoVO> photos = fetchMarsPhotos(PERSEVERANCE_URL + nasaApiKey, "Perseverance");

        // 2. Perseverance 无数据，降级到 Curiosity
        if (photos.isEmpty()) {
            log.info("[NasaApiService] Perseverance 暂无照片，降级到 Curiosity");
            photos = fetchMarsPhotos(CURIOSITY_URL + nasaApiKey, "Curiosity");
        }

        log.info("[NasaApiService] 最终获取到 {} 张火星照片", photos.size());
        return photos;
    }

    // ============================================================
    // 私有工具方法
    // ============================================================

    /**
     * 调用指定 Rover 的 latest_photos 接口，返回前3张
     *
     * @param url      完整 API URL（含 api_key 参数）
     * @param roverName Rover 名称（仅用于日志）
     * @return 照片列表（最多3张），失败返回空列表
     */
    @SuppressWarnings("unchecked")
    private List<MarsPhotoVO> fetchMarsPhotos(String url, String roverName) {
        List<MarsPhotoVO> result = new ArrayList<>();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) {
                log.warn("[NasaApiService] {} API 返回 body 为 null", roverName);
                return result;
            }

            List<Map<String, Object>> latestPhotos =
                    (List<Map<String, Object>>) body.get("latest_photos");

            if (latestPhotos == null || latestPhotos.isEmpty()) {
                log.info("[NasaApiService] {} 暂无最新照片", roverName);
                return result;
            }

            // 取前3张
            int limit = Math.min(3, latestPhotos.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> photoMap = latestPhotos.get(i);
                MarsPhotoVO vo = new MarsPhotoVO();
                vo.setImgSrc((String) photoMap.get("img_src"));
                vo.setEarthDate((String) photoMap.get("earth_date"));

                // 提取摄像头全称 camera.full_name
                Object cameraObj = photoMap.get("camera");
                if (cameraObj instanceof Map) {
                    Map<String, Object> camera = (Map<String, Object>) cameraObj;
                    vo.setCameraFullName((String) camera.get("full_name"));
                }

                result.add(vo);
            }

            log.info("[NasaApiService] {} 成功获取 {} 张照片，地球日期: {}",
                    roverName, result.size(),
                    result.isEmpty() ? "N/A" : result.get(0).getEarthDate());

        } catch (Exception e) {
            log.error("[NasaApiService] 调用 {} Mars API 失败: {}", roverName, e.getMessage(), e);
        }
        return result;
    }
}
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
import java.time.format.DateTimeFormatter;
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
 * 📌 版本变更说明 (5.2):
 * - 新增 getApodByDate(date)        管理员批量补录历史 APOD，不走缓存
 * - 新增 getAllLatestMarsPhotos()    MarsRoverSyncScheduler 专用，返回全量照片（不限3张）
 * - 提取 parseApodResponse()        消除 getTodayApod/getApodByDate 的重复解析逻辑
 * - 提取 parseMarsPhotoList()       消除 getLatestMarsPhotos/getAllLatestMarsPhotos 的重复逻辑
 *
 * @see NasaApiService
 */
@Slf4j
@Service
public class NasaApiServiceImpl implements NasaApiService {

    // ============================================================
    // 常量：NASA API 基础 URL
    // ============================================================

    /** NASA APOD API 地址（不含 date 参数，今日 APOD 直接请求） */
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
     * 2. 未命中 → 调用 NASA APOD API（无 date 参数） → 存入缓存
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

        log.info("[NasaApiService] 调用 NASA APOD API（今日），日期: {}", today);
        try {
            String url = APOD_URL + nasaApiKey;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) {
                log.warn("[NasaApiService] APOD API 返回 body 为 null");
                return null;
            }

            ApodVO vo = parseApodResponse(body);

            // 存入当日缓存
            todayApodCache = vo;
            apodCacheDate = today;

            log.info("[NasaApiService] APOD 获取成功并缓存，标题: {}", vo.getTitle());
            return vo;

        } catch (Exception e) {
            log.error("[NasaApiService] 调用 NASA APOD API 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取指定日期的历史 APOD（管理员批量补录用，不走缓存）
     *
     * 📌 与 getTodayApod() 的区别：每次都直接请求 NASA API，适合历史数据补录
     * 📌 NASA APOD 历史最早日期：1995-06-16
     *
     * @param date 指定日期（LocalDate，不能晚于今天）
     * @return ApodVO，失败返回 null
     */
    @Override
    public ApodVO getApodByDate(LocalDate date) {
        if (date == null) {
            log.warn("[NasaApiService] getApodByDate: date 参数为 null");
            return null;
        }
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 不走缓存，直接构造带 date 参数的 URL
        String url = APOD_URL + nasaApiKey + "&date=" + dateStr;

        log.info("[NasaApiService] 调用 NASA APOD API（历史），日期: {}", dateStr);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) {
                log.warn("[NasaApiService] APOD[{}] API 返回 body 为 null", dateStr);
                return null;
            }

            ApodVO vo = parseApodResponse(body);
            log.info("[NasaApiService] 历史 APOD[{}] 获取成功，标题: {}", dateStr, vo.getTitle());
            return vo;

        } catch (Exception e) {
            log.error("[NasaApiService] 获取历史 APOD[{}] 失败: {}", dateStr, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取火星车最新照片，取前3张（NasaController 展示用）
     *
     * 📌 降级策略：先 Perseverance，空列表则切换 Curiosity
     *
     * @return 最多3张 MarsPhotoVO，可能为空列表
     */
    @Override
    public List<MarsPhotoVO> getLatestMarsPhotos() {
        log.info("[NasaApiService] 获取火星车最新照片（展示版，最多3张）...");

        // 1. 尝试 Perseverance（限制3张）
        List<MarsPhotoVO> photos = fetchMarsPhotos(PERSEVERANCE_URL + nasaApiKey, "Perseverance", 3);

        // 2. Perseverance 无数据，降级到 Curiosity
        if (photos.isEmpty()) {
            log.info("[NasaApiService] Perseverance 暂无照片，降级到 Curiosity");
            photos = fetchMarsPhotos(CURIOSITY_URL + nasaApiKey, "Curiosity", 3);
        }

        log.info("[NasaApiService] 最终获取到 {} 张火星照片（展示版）", photos.size());
        return photos;
    }

    /**
     * 获取火星车最新照片，全量返回（MarsRoverSyncScheduler 专用）
     *
     * ⚠️ 不限制数量，NASA latest_photos 接口最多返回当天全部照片（可能数十到数百张）
     * 📌 降级策略：Perseverance → Curiosity（与 getLatestMarsPhotos 相同）
     *
     * @return 全量照片列表（最多200张限制，防止超大响应），可能为空列表
     */
    @Override
    public List<MarsPhotoVO> getAllLatestMarsPhotos() {
        log.info("[NasaApiService] 获取火星车最新照片（全量版，MarsRoverSyncScheduler专用）...");

        // 1. 尝试 Perseverance（不限数量，内部限制200防止过大）
        List<MarsPhotoVO> photos = fetchMarsPhotos(PERSEVERANCE_URL + nasaApiKey, "Perseverance", 200);

        // 2. Perseverance 无数据，降级到 Curiosity
        if (photos.isEmpty()) {
            log.info("[NasaApiService] Perseverance 暂无照片，降级到 Curiosity（全量版）");
            photos = fetchMarsPhotos(CURIOSITY_URL + nasaApiKey, "Curiosity", 200);
        }

        log.info("[NasaApiService] 最终获取到 {} 张火星照片（全量版）", photos.size());
        return photos;
    }

    // ============================================================
    // 私有工具方法
    // ============================================================

    /**
     * 解析 NASA APOD API 响应体，组装 ApodVO
     *
     * 📌 提取为公共方法，同时被 getTodayApod() 和 getApodByDate() 复用
     *
     * @param body NASA API 响应的 Map 格式数据
     * @return ApodVO
     */
    private ApodVO parseApodResponse(Map<String, Object> body) {
        ApodVO vo = new ApodVO();
        vo.setDate((String) body.get("date"));
        vo.setTitle((String) body.get("title"));
        vo.setExplanation((String) body.get("explanation"));
        vo.setUrl((String) body.get("url"));
        vo.setHdurl((String) body.get("hdurl"));
        // NASA 返回字段名是 media_type，Java 对象用 camelCase
        vo.setMediaType((String) body.get("media_type"));
        vo.setCopyright((String) body.get("copyright"));
        return vo;
    }

    /**
     * 调用指定 Rover 的 latest_photos 接口，返回最多 limit 张照片
     *
     * 📌 getLatestMarsPhotos() 传 limit=3（展示用）
     * 📌 getAllLatestMarsPhotos() 传 limit=200（同步用）
     *
     * @param url       完整 API URL（含 api_key 参数）
     * @param roverName Rover 名称（仅用于日志）
     * @param limit     最多返回条数（防止 response 过大）
     * @return 照片列表，失败返回空列表
     */
    @SuppressWarnings("unchecked")
    private List<MarsPhotoVO> fetchMarsPhotos(String url, String roverName, int limit) {
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

            // 按 limit 截断，防止超大响应
            int actualLimit = Math.min(limit, latestPhotos.size());
            for (int i = 0; i < actualLimit; i++) {
                Map<String, Object> photoMap = latestPhotos.get(i);
                MarsPhotoVO vo = parseMarsPhotoMap(photoMap);
                if (vo != null) {
                    result.add(vo);
                }
            }

            log.info("[NasaApiService] {} 成功获取 {} 张照片（请求 limit={}），地球日期: {}",
                    roverName, result.size(), limit,
                    result.isEmpty() ? "N/A" : result.get(0).getEarthDate());

        } catch (Exception e) {
            log.error("[NasaApiService] 调用 {} Mars API 失败: {}", roverName, e.getMessage(), e);
        }
        return result;
    }

    /**
     * 解析单条火星照片 Map，组装 MarsPhotoVO
     *
     * 📌 提取为独立方法，供 fetchMarsPhotos() 复用
     *
     * @param photoMap NASA API 返回的单条照片 Map
     * @return MarsPhotoVO，imgSrc 为 null 时返回 null（过滤无效数据）
     */
    @SuppressWarnings("unchecked")
    private MarsPhotoVO parseMarsPhotoMap(Map<String, Object> photoMap) {
        if (photoMap == null) {
            return null;
        }
        String imgSrc = (String) photoMap.get("img_src");
        if (imgSrc == null || imgSrc.isEmpty()) {
            // 没有图片 URL 的数据无意义，跳过
            return null;
        }

        MarsPhotoVO vo = new MarsPhotoVO();
        vo.setImgSrc(imgSrc);
        vo.setEarthDate((String) photoMap.get("earth_date"));

        // 提取摄像头全称 camera.full_name
        Object cameraObj = photoMap.get("camera");
        if (cameraObj instanceof Map) {
            Map<String, Object> camera = (Map<String, Object>) cameraObj;
            vo.setCameraFullName((String) camera.get("full_name"));
        }

        return vo;
    }
}
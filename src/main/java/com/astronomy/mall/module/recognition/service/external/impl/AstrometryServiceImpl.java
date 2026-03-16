package com.astronomy.mall.module.recognition.service.external.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.astronomy.mall.module.recognition.entity.Recognition;
import com.astronomy.mall.module.recognition.mapper.RecognitionMapper;
import com.astronomy.mall.module.recognition.service.external.AstrometryService;
import com.astronomy.mall.module.recognition.service.external.dto.AstrometryJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Astrometry.net 外部 API 实现类（完整版 v4.2）
 *
 * 📌 4.1 已实现: getSessionKey, submitAsync
 * 📌 4.2 新增:   getSubmissionJobId, getJobStatus, getJobCalibration,
 *                getJobInfo, buildAnnotatedImageUrl
 *
 * 关键设计:
 *   - Session 内存缓存 30 分钟，避免频繁登录
 *   - submitAsync 用 @Async 隔离主线程
 *   - 所有 GET 请求统一加 Referer 头（防爬虫拦截）
 *   - HTTP 请求失败不抛异常，返回 null，由调用方（Scheduler）决策
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AstrometryServiceImpl implements AstrometryService {

    // ============================================================
    // 注入
    // ============================================================

    @Value("${astrometry.api-key}")
    private String apiKey;

    @Value("${astrometry.base-url}")
    private String baseUrl;

    /** 识别专用 RestTemplate（120s 超时），Bean 名称匹配字段名自动注入 */
    private final RestTemplate recognitionRestTemplate;

    private final RecognitionMapper recognitionMapper;

    // ============================================================
    // Session 缓存
    // ============================================================

    private volatile String cachedSession = null;
    private volatile long sessionExpireAt = 0;
    private static final long SESSION_TTL_MS = 30 * 60 * 1000L;

    // ============================================================
    // =================== v4.1 方法 ==============================
    // ============================================================

    @Override
    public String getSessionKey() {
        if (cachedSession != null && System.currentTimeMillis() < sessionExpireAt) {
            return cachedSession;
        }
        return doLogin();
    }

    @Async("recognitionExecutor")
    @Override
    public void submitAsync(Long recognitionId) {
        log.info("[Astrometry] 开始异步提交, recognitionId={}", recognitionId);
        try {
            Recognition recognition = recognitionMapper.selectById(recognitionId);
            if (recognition == null) {
                log.error("[Astrometry] 识别记录不存在, id={}", recognitionId);
                return;
            }

            byte[] imageBytes = decodeImageData(recognition.getImageData());
            if (imageBytes == null) {
                updateFailed(recognitionId, "图片解码失败");
                return;
            }

            String session;
            try {
                session = getSessionKey();
            } catch (Exception e) {
                updateFailed(recognitionId, "Astrometry 登录失败: " + e.getMessage());
                return;
            }

            String submissionId;
            try {
                submissionId = uploadImage(session, imageBytes, "star_" + recognitionId + ".jpg");
            } catch (Exception e) {
                updateFailed(recognitionId, "图片上传失败: " + e.getMessage());
                return;
            }

            recognitionMapper.updateSubmissionId(recognitionId, submissionId);
            log.info("[Astrometry] 提交成功, recognitionId={}, submissionId={}", recognitionId, submissionId);

        } catch (Exception e) {
            log.error("[Astrometry] 异步任务异常, recognitionId={}", recognitionId, e);
            updateFailed(recognitionId, "系统异常: " + e.getMessage());
        }
    }

    // ============================================================
    // =================== v4.2 方法 ==============================
    // ============================================================

    /**
     * 查询 submission 状态，获取 job_id
     *
     * GET /api/submissions/{submissionId}
     * 响应: { "jobs": [12345678], "status": "Complete", ... }
     *
     * ⚠️ jobs 数组为空时说明 submission 还未分配 job，返回 null
     */
    @Override
    public String getSubmissionJobId(String submissionId) {
        String url = baseUrl + "/submissions/" + submissionId;
        log.debug("[Astrometry] 查询 submission: {}", submissionId);

        try {
            String body = doGet(url);
            if (body == null) return null;

            JSONObject json = JSON.parseObject(body);
            JSONArray jobs = json.getJSONArray("jobs");

            if (jobs == null || jobs.isEmpty()) {
                log.debug("[Astrometry] submission {} 尚未分配 job", submissionId);
                return null;
            }

            // 取第一个非 null 的 job_id
            for (int i = 0; i < jobs.size(); i++) {
                Object jobId = jobs.get(i);
                if (jobId != null) {
                    String result = String.valueOf(jobId);
                    log.info("[Astrometry] submission {} → job_id: {}", submissionId, result);
                    return result;
                }
            }
            return null;

        } catch (Exception e) {
            log.warn("[Astrometry] 查询 submission 失败, subId={}: {}", submissionId, e.getMessage());
            return null;
        }
    }

    /**
     * 查询 job 状态
     *
     * GET /api/jobs/{jobId}
     * 响应: { "status": "success" | "solving" | "failure" }
     */
    @Override
    public String getJobStatus(String jobId) {
        String url = baseUrl + "/jobs/" + jobId;
        log.debug("[Astrometry] 查询 job 状态: {}", jobId);

        try {
            String body = doGet(url);
            if (body == null) return "solving"; // 网络异常时当作还在解析

            JSONObject json = JSON.parseObject(body);
            String status = json.getString("status");
            log.info("[Astrometry] job {} 状态: {}", jobId, status);
            return status != null ? status : "solving";

        } catch (Exception e) {
            log.warn("[Astrometry] 查询 job 状态失败, jobId={}: {}", jobId, e.getMessage());
            return "solving";
        }
    }

    /**
     * 获取 job 坐标校准数据
     *
     * GET /api/jobs/{jobId}/calibration
     * 响应: { "ra": 83.82, "dec": -5.39, "radius": 0.58, "orientation": 179.22, "pixscale": 1.056 }
     */
    @Override
    public AstrometryJobResult getJobCalibration(String jobId) {
        String url = baseUrl + "/jobs/" + jobId + "/calibration";
        log.debug("[Astrometry] 获取 calibration, jobId={}", jobId);

        try {
            String body = doGet(url);
            if (body == null) return null;

            JSONObject json = JSON.parseObject(body);
            AstrometryJobResult result = new AstrometryJobResult();

            // 安全取值（防止字段缺失或类型不匹配）
            result.setRa(getBigDecimal(json, "ra"));
            result.setDec(getBigDecimal(json, "dec"));
            result.setOrientation(getBigDecimal(json, "orientation"));
            result.setRadius(getBigDecimal(json, "radius"));
            result.setPixscale(getBigDecimal(json, "pixscale"));

            log.info("[Astrometry] calibration: ra={}, dec={}, radius={}, orientation={}",
                    result.getRa(), result.getDec(), result.getRadius(), result.getOrientation());
            return result;

        } catch (Exception e) {
            log.warn("[Astrometry] 获取 calibration 失败, jobId={}: {}", jobId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取 job 天体信息
     *
     * GET /api/jobs/{jobId}/info
     * 响应: { "objects_in_field": ["Orion Nebula", ...], "machine_tags": ["nebula", ...] }
     */
    @Override
    public AstrometryJobResult getJobInfo(String jobId) {
        String url = baseUrl + "/jobs/" + jobId + "/info";
        log.debug("[Astrometry] 获取 job info, jobId={}", jobId);

        try {
            String body = doGet(url);
            if (body == null) return null;

            JSONObject json = JSON.parseObject(body);
            AstrometryJobResult result = new AstrometryJobResult();

            // objects_in_field
            JSONArray objectsArr = json.getJSONArray("objects_in_field");
            List<String> objects = new ArrayList<>();
            if (objectsArr != null) {
                for (int i = 0; i < objectsArr.size(); i++) {
                    String obj = objectsArr.getString(i);
                    if (obj != null) objects.add(obj);
                }
            }
            result.setObjectsInField(objects);

            // machine_tags
            JSONArray tagsArr = json.getJSONArray("machine_tags");
            List<String> tags = new ArrayList<>();
            if (tagsArr != null) {
                for (int i = 0; i < tagsArr.size(); i++) {
                    String tag = tagsArr.getString(i);
                    if (tag != null) tags.add(tag);
                }
            }
            result.setMachineTags(tags);

            log.info("[Astrometry] job info: objects={}, tags={}", objects.size(), tags.size());
            return result;

        } catch (Exception e) {
            log.warn("[Astrometry] 获取 job info 失败, jobId={}: {}", jobId, e.getMessage());
            return null;
        }
    }

    /**
     * 构建标注图片 URL
     *
     * 格式: https://nova.astrometry.net/annotated_display/{jobId}
     */
    @Override
    public String buildAnnotatedImageUrl(String jobId) {
        // ⚠️ 此 URL 不走 baseUrl（baseUrl 为 /api 前缀路径），直接拼主域名
        return "https://nova.astrometry.net/annotated_display/" + jobId;
    }

    // ============================================================
    // 私有方法
    // ============================================================

    /**
     * 登录获取新的 session key
     *
     * POST /api/login
     * Body: request-json={"apikey":"xxx"}（application/x-www-form-urlencoded）
     */
    private String doLogin() {
        log.info("[Astrometry] 执行登录...");
        String requestJson = "{\"apikey\":\"" + apiKey + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("request-json", requestJson);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<String> response;
        try {
            response = recognitionRestTemplate.postForEntity(
                    baseUrl + "/login", request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("登录请求失败: " + e.getMessage(), e);
        }

        JSONObject result = parseAndCheckResponse(response.getBody(), "login");
        String session = result.getString("session");
        if (session == null || session.isEmpty()) {
            throw new RuntimeException("登录响应中未找到 session");
        }

        this.cachedSession = session;
        this.sessionExpireAt = System.currentTimeMillis() + SESSION_TTL_MS;
        log.info("[Astrometry] 登录成功");
        return session;
    }

    /**
     * 上传图片，获取 submission_id
     *
     * POST /api/upload，multipart/form-data
     * 字段: request-json (含 session) + file (图片)
     */
    private String uploadImage(String session, byte[] imageBytes, String filename) {
        log.info("[Astrometry] 上传图片, filename={}, size={}KB",
                filename, imageBytes.length / 1024);

        String requestJson = String.format(
                "{\"session\":\"%s\",\"allow_commercial_use\":\"d\"," +
                        "\"allow_modifications\":\"d\",\"publicly_visible\":\"n\"}",
                session);

        ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);

        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("request-json", requestJson);
        multipartBody.add("file", filePart);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Referer", "https://nova.astrometry.net/api/login");

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multipartBody, headers);

        ResponseEntity<String> response;
        try {
            response = recognitionRestTemplate.postForEntity(
                    baseUrl + "/upload", request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("上传请求失败: " + e.getMessage(), e);
        }

        JSONObject result = parseAndCheckResponse(response.getBody(), "upload");
        Object subidObj = result.get("subid");
        if (subidObj == null) {
            throw new RuntimeException("上传响应中未找到 subid: " + response.getBody());
        }
        return String.valueOf(subidObj);
    }

    /**
     * 执行带 Referer 头的 GET 请求
     *
     * ⚠️ Astrometry.net 所有 GET 接口下载时需要 Referer，统一在此处理
     *
     * @param url 完整 URL
     * @return 响应体字符串，请求失败返回 null
     */
    private String doGet(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            // ⚠️ 必须携带 Referer，否则被反爬虫拦截
            headers.set("Referer", "https://nova.astrometry.net/api/login");
            headers.set("User-Agent", "AstronomyMall/1.0");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = recognitionRestTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            return response.getBody();
        } catch (Exception e) {
            log.warn("[Astrometry] GET 请求失败, url={}: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 解析 Astrometry API 响应，status != "success" 时抛异常
     */
    private JSONObject parseAndCheckResponse(String body, String apiName) {
        if (body == null || body.isEmpty()) {
            throw new RuntimeException("[" + apiName + "] 空响应");
        }
        log.debug("[Astrometry][{}] 响应: {}", apiName,
                body.length() > 200 ? body.substring(0, 200) + "..." : body);

        JSONObject json;
        try {
            json = JSON.parseObject(body);
        } catch (Exception e) {
            throw new RuntimeException("[" + apiName + "] 响应非 JSON: " + body);
        }

        String status = json.getString("status");
        if (!"success".equals(status)) {
            String err = json.getString("errormessage");
            throw new RuntimeException("[" + apiName + "] 失败: " + (err != null ? err : body));
        }
        return json;
    }

    /** 从 JSONObject 安全取 BigDecimal */
    private BigDecimal getBigDecimal(JSONObject json, String key) {
        Object val = json.get(key);
        if (val == null) return null;
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (Exception e) {
            return null;
        }
    }

    /** 解码 base64 图片（容错含 data: 前缀的情况） */
    private byte[] decodeImageData(String imageData) {
        if (imageData == null || imageData.isEmpty()) return null;
        try {
            String base64 = imageData.contains(",")
                    ? imageData.substring(imageData.indexOf(',') + 1)
                    : imageData;
            return Base64.getDecoder().decode(base64.trim());
        } catch (Exception e) {
            log.error("[Astrometry] base64 解码失败: {}", e.getMessage());
            return null;
        }
    }

    /** 将识别记录更新为失败状态 */
    private void updateFailed(Long recognitionId, String reason) {
        log.warn("[Astrometry] 任务失败, id={}, reason={}", recognitionId, reason);
        try {
            recognitionMapper.updateFailed(recognitionId, reason);
        } catch (Exception e) {
            log.error("[Astrometry] 更新失败状态出错, id={}", recognitionId, e);
        }
    }
}
package com.astronomy.mall.module.recognition.service.external;

import com.astronomy.mall.module.recognition.service.external.dto.AstrometryJobResult;

/**
 * Astrometry.net 外部 API 封装接口
 *
 * 📌 版本说明:
 *   v4.1 新增: getSessionKey, submitAsync
 *   v4.2 新增: getSubmissionJobId, getJobStatus, getJobCalibration,
 *              getJobInfo, buildAnnotatedImageUrl
 *
 * Astrometry.net 识别完整链路:
 *
 *   [submitAsync]  上传图片 → 返回 submission_id (4.1完成)
 *        ↓ 每30秒定时轮询 (4.2新增)
 *   [getSubmissionJobId]  GET /api/submissions/{subId} → 获取 job_id
 *        ↓
 *   [getJobStatus]  GET /api/jobs/{jobId} → "success"/"solving"/"failure"
 *        ↓ 成功时并行调用
 *   [getJobCalibration]  GET /api/jobs/{jobId}/calibration → ra/dec/orientation/radius
 *   [getJobInfo]         GET /api/jobs/{jobId}/info        → objects_in_field/machine_tags
 *        ↓
 *   [buildAnnotatedImageUrl]  https://nova.astrometry.net/annotated_display/{jobId}
 */
public interface AstrometryService {

    // ============================================================
    // v4.1 方法
    // ============================================================

    /**
     * 获取有效的 session key（内部缓存，失效自动刷新）
     */
    String getSessionKey();

    /**
     * 异步提交识别任务（@Async，不阻塞主线程）
     *
     * @param recognitionId 识别记录 ID
     */
    void submitAsync(Long recognitionId);

    // ============================================================
    // v4.2 方法（定时轮询任务调用）
    // ============================================================

    /**
     * 查询 submission 状态，返回 job_id（若已处理完成）
     *
     * GET https://nova.astrometry.net/api/submissions/{submissionId}
     *
     * 响应示例:
     *   { "jobs": [12345678], "status": "Complete" }
     *
     * ⚠️ jobs 数组可能为空（submission 还在排队），此时返回 null
     *
     * @param submissionId Astrometry submission_id
     * @return job_id 字符串，若尚未分配则返回 null
     */
    String getSubmissionJobId(String submissionId);

    /**
     * 查询 job 当前状态
     *
     * GET https://nova.astrometry.net/api/jobs/{jobId}
     * 响应: { "status": "success" | "solving" | "failure" }
     *
     * @param jobId Astrometry job_id
     * @return "success" / "solving" / "failure"
     */
    String getJobStatus(String jobId);

    /**
     * 查询 job 坐标校准结果（仅 status=success 时调用）
     *
     * GET https://nova.astrometry.net/api/jobs/{jobId}/calibration
     * 响应示例:
     *   { "ra": 83.8221, "dec": -5.3911, "radius": 0.5791, "orientation": 179.22 }
     *
     * @param jobId Astrometry job_id
     * @return 包含坐标数据的 DTO，失败返回 null
     */
    AstrometryJobResult getJobCalibration(String jobId);

    /**
     * 查询 job 天体信息（仅 status=success 时调用）
     *
     * GET https://nova.astrometry.net/api/jobs/{jobId}/info
     * 响应示例:
     *   { "objects_in_field": ["Orion Nebula","M42"], "machine_tags": ["nebula","emission"] }
     *
     * @param jobId Astrometry job_id
     * @return 包含天体信息的 DTO，失败返回 null
     */
    AstrometryJobResult getJobInfo(String jobId);

    /**
     * 构建标注图片 URL
     *
     * URL: https://nova.astrometry.net/annotated_display/{jobId}
     * ⚠️ 前端直接用此 URL 作为 img src；后端下载时需加 Referer 请求头
     *
     * @param jobId Astrometry job_id
     * @return 标注图片完整 URL
     */
    String buildAnnotatedImageUrl(String jobId);
}
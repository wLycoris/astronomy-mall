package com.astronomy.mall.module.recognition.task;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.recognition.entity.Recognition;
import com.astronomy.mall.module.recognition.mapper.RecognitionMapper;
import com.astronomy.mall.module.recognition.service.external.AstrometryService;
import com.astronomy.mall.module.recognition.service.external.dto.AstrometryJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 星图识别结果轮询定时任务
 *
 * 📌 触发条件: 每 30 秒执行一次
 * 📌 处理对象: 所有 status=0 且 submission_id 不为空的识别记录
 * 📌 2026-03-16 新增: 识别成功/失败后发送通知
 *
 * 轮询流程（针对每条待处理记录）:
 * ┌──────────────────────────────────────────────────┐
 * │ 1. 超时检查：超过 10 分钟 → 标记失败 + 发失败通知  │
 * │ 2. 获取 job_id（若当前 job_id 为空）               │
 * │ 3. 查询 job 状态                                  │
 * │    → "solving"  → 跳过                            │
 * │    → "failure"  → 标记失败 + 发失败通知            │
 * │    → "success"  → 写入结果 + 发成功通知            │
 * └──────────────────────────────────────────────────┘
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecognitionPollScheduler {

    private final RecognitionMapper recognitionMapper;
    private final AstrometryService astrometryService;
    private final NotificationHelper notificationHelper;  // 🆕

    private static final int TIMEOUT_MINUTES = 10;

    // ============================================================
    // 定时任务入口
    // ============================================================

    @Scheduled(fixedDelay = 30000, initialDelay = 60000)
    public void pollPendingRecognitions() {
        List<Recognition> pendingList = recognitionMapper.selectPendingList();
        if (pendingList == null || pendingList.isEmpty()) {
            return;
        }

        log.info("[PollScheduler] 开始轮询，待处理记录数: {}", pendingList.size());
        for (Recognition recognition : pendingList) {
            try {
                processSingleRecord(recognition);
            } catch (Exception e) {
                log.error("[PollScheduler] 处理记录异常, id={}", recognition.getId(), e);
            }
        }
        log.info("[PollScheduler] 本轮轮询完成");
    }

    // ============================================================
    // 单条记录处理
    // ============================================================

    private void processSingleRecord(Recognition recognition) {
        Long id = recognition.getId();
        String submissionId = recognition.getSubmissionId();

        // Step 1: 超时检查
        if (isTimeout(recognition)) {
            log.warn("[PollScheduler] 识别超时, id={}", id);
            String reason = "识别超时（超过 " + TIMEOUT_MINUTES + " 分钟）";
            recognitionMapper.updateFailed(id, reason);
            notifyFailed(recognition, reason);  // 🆕
            return;
        }

        // Step 2: 获取 job_id
        String jobId = recognition.getJobId();
        if (jobId == null || jobId.isEmpty()) {
            jobId = astrometryService.getSubmissionJobId(submissionId);
            if (jobId == null) {
                log.debug("[PollScheduler] submission {} 尚未分配 job，等待下次轮询", submissionId);
                return;
            }
            recognitionMapper.updateJobId(id, jobId);
            log.info("[PollScheduler] 获得 job_id={}, recognitionId={}", jobId, id);
        }

        // Step 3: 查询 job 状态
        String jobStatus = astrometryService.getJobStatus(jobId);
        log.debug("[PollScheduler] job {} 状态: {}", jobId, jobStatus);

        switch (jobStatus) {
            case "solving":
                log.debug("[PollScheduler] job {} 仍在解析，等待...", jobId);
                return;

            case "failure":
                log.info("[PollScheduler] job {} 识别失败", jobId);
                String reason = "图片中未能检测到足够的星点，请尝试其他照片";
                recognitionMapper.updateFailed(id, reason);
                notifyFailed(recognition, reason);  // 🆕
                return;

            case "success":
                log.info("[PollScheduler] job {} 识别成功！获取结果...", jobId);
                handleSuccess(id, jobId, recognition.getUserId());
                return;

            default:
                log.warn("[PollScheduler] job {} 未知状态: {}", jobId, jobStatus);
        }
    }

    /**
     * 识别成功：写入结果 + 发成功通知
     */
    private void handleSuccess(Long recognitionId, String jobId, Long userId) {
        AstrometryJobResult calibration = astrometryService.getJobCalibration(jobId);
        AstrometryJobResult jobInfo = astrometryService.getJobInfo(jobId);
        String resultImageUrl = astrometryService.buildAnnotatedImageUrl(jobId);

        Recognition updateEntity = new Recognition();
        updateEntity.setId(recognitionId);
        updateEntity.setJobId(jobId);
        updateEntity.setStatus(1);
        updateEntity.setResultImageUrl(resultImageUrl);

        if (calibration != null) {
            updateEntity.setRa(calibration.getRa());
            updateEntity.setDec(calibration.getDec());
            updateEntity.setOrientation(calibration.getOrientation());
            updateEntity.setRadius(calibration.getRadius());
        }
        if (jobInfo != null) {
            if (jobInfo.getObjectsInField() != null) {
                updateEntity.setObjectsInField(JSON.toJSONString(jobInfo.getObjectsInField()));
            }
            if (jobInfo.getMachineTags() != null) {
                updateEntity.setMachineTags(JSON.toJSONString(jobInfo.getMachineTags()));
            }
        }

        recognitionMapper.updateResult(updateEntity);
        log.info("[PollScheduler] 识别结果已写入, recognitionId={}", recognitionId);

        // 🆕 发送识别成功通知
        if (userId != null) {
            try {
                String objectNames = buildObjectNames(jobInfo);
                notificationHelper.sendRecognitionCompletedNotification(userId, objectNames, recognitionId);
            } catch (Exception e) {
                log.warn("[PollScheduler] 发送识别成功通知失败, recognitionId={}", recognitionId, e);
            }
        }
    }

    /**
     * 发送识别失败通知（统一入口）
     */
    private void notifyFailed(Recognition recognition, String reason) {
        if (recognition.getUserId() == null) return;
        try {
            notificationHelper.sendRecognitionFailedNotification(
                    recognition.getUserId(), reason, recognition.getId());
        } catch (Exception e) {
            log.warn("[PollScheduler] 发送识别失败通知异常, recognitionId={}", recognition.getId(), e);
        }
    }

    /**
     * 拼接天体名称展示字符串，最多取前3个
     * 示例: "猎户座星云、M42、NGC 1976 等5个天体"
     */
    private String buildObjectNames(AstrometryJobResult jobInfo) {
        if (jobInfo == null || jobInfo.getObjectsInField() == null
                || jobInfo.getObjectsInField().isEmpty()) {
            return "未知天体";
        }
        List<String> objects = jobInfo.getObjectsInField();
        int limit = Math.min(objects.size(), 3);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append("、");
            sb.append(objects.get(i));
        }
        if (objects.size() > 3) {
            sb.append(" 等").append(objects.size()).append("个天体");
        }
        return sb.toString();
    }

    // ============================================================
    // 工具方法
    // ============================================================

    private boolean isTimeout(Recognition recognition) {
        if (recognition.getCreateTime() == null) return false;
        long minutes = ChronoUnit.MINUTES.between(recognition.getCreateTime(), LocalDateTime.now());
        return minutes > TIMEOUT_MINUTES;
    }
}
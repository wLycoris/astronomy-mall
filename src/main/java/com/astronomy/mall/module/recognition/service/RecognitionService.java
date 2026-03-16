package com.astronomy.mall.module.recognition.service;

import com.astronomy.mall.module.recognition.dto.SubmitRecognitionDTO;
import com.astronomy.mall.module.recognition.vo.RecognitionVO;

import java.util.List;
import java.util.Map;

/**
 * AI星图识别业务服务接口
 *
 * 📌 v4.1 新增: submit, getStatus
 * 📌 v4.2 新增: getDetail, getHistory
 */
public interface RecognitionService {

    // ============================================================
    // v4.1 方法
    // ============================================================

    /**
     * 提交星图识别任务
     *
     * 1. 保存图片和初始记录（status=0）
     * 2. 触发 @Async 异步上传到 Astrometry.net
     * 3. 立即返回 recognitionId
     *
     * @param dto    含 imageData 的请求 DTO
     * @param userId 当前用户 ID
     * @return 含 recognitionId 和 status=0 的 VO
     */
    RecognitionVO submit(SubmitRecognitionDTO dto, Long userId);

    /**
     * 查询识别状态（等待页轮询）
     *
     * @param recognitionId 识别记录 ID
     * @param userId        当前用户 ID（鉴权）
     * @return 当前状态 VO
     */
    RecognitionVO getStatus(Long recognitionId, Long userId);

    // ============================================================
    // v4.2 方法
    // ============================================================

    /**
     * 获取识别详情（结果页使用）
     *
     * 包含全部识别结果字段，但不含 imageData（前端本地已有）。
     *
     * @param recognitionId 识别记录 ID
     * @param userId        当前用户 ID（鉴权）
     * @return 完整识别结果 VO
     */
    RecognitionVO getDetail(Long recognitionId, Long userId);

    /**
     * 查询用户历史识别记录（分页）
     *
     * @param userId  当前用户 ID
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页数量（默认 10）
     * @return Map 含 list（记录列表）和 total（总数）
     */
    Map<String, Object> getHistory(Long userId, int pageNum, int pageSize);
}
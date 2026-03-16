package com.astronomy.mall.module.recognition.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.recognition.dto.SubmitRecognitionDTO;
import com.astronomy.mall.module.recognition.entity.Recognition;
import com.astronomy.mall.module.recognition.mapper.RecognitionMapper;
import com.astronomy.mall.module.recognition.service.RecognitionService;
import com.astronomy.mall.module.recognition.service.external.AstrometryService;
import com.astronomy.mall.module.recognition.vo.RecognitionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI星图识别业务服务实现类
 *
 * 📌 v4.1: submit, getStatus
 * 📌 v4.2: getDetail, getHistory
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecognitionServiceImpl implements RecognitionService {

    private final RecognitionMapper recognitionMapper;
    private final AstrometryService astrometryService;

    // ============================================================
    // v4.1: submit
    // ============================================================

    @Override
    public RecognitionVO submit(SubmitRecognitionDTO dto, Long userId) {
        log.info("[Recognition] 用户 {} 提交星图识别", userId);

        // 1. 创建初始记录（status=0）
        Recognition recognition = new Recognition();
        recognition.setUserId(userId);
        recognition.setImageData(dto.getImageData());
        recognition.setStatus(0);
        recognitionMapper.insert(recognition);

        Long recognitionId = recognition.getId();
        log.info("[Recognition] 记录已创建, id={}", recognitionId);

        // 2. 触发异步提交（通过注入的 Bean 调用，保证 @Async 代理生效）
        try {
            astrometryService.submitAsync(recognitionId);
        } catch (Exception e) {
            log.error("[Recognition] 触发异步任务失败, id={}", recognitionId, e);
        }

        // 3. 立即返回
        RecognitionVO vo = new RecognitionVO();
        vo.setId(recognitionId);
        vo.setStatus(0);
        vo.setCreateTime(recognition.getCreateTime());
        return vo;
    }

    // ============================================================
    // v4.1: getStatus（等待页轮询）
    // ============================================================

    @Override
    public RecognitionVO getStatus(Long recognitionId, Long userId) {
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        checkOwnership(recognition, recognitionId, userId);
        return convertToVO(recognition);
    }

    // ============================================================
    // v4.2: getDetail（结果页）
    // ============================================================

    @Override
    public RecognitionVO getDetail(Long recognitionId, Long userId) {
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        checkOwnership(recognition, recognitionId, userId);
        return convertToVO(recognition);
    }

    // ============================================================
    // v4.2: getHistory（历史记录列表）
    // ============================================================

    @Override
    public Map<String, Object> getHistory(Long userId, int pageNum, int pageSize) {
        // 参数合法性修正
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 10;

        int offset = (pageNum - 1) * pageSize;

        List<Recognition> records = recognitionMapper.selectHistoryByUserId(userId, offset, pageSize);
        int total = recognitionMapper.countHistoryByUserId(userId);

        List<RecognitionVO> voList = records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    // ============================================================
    // 私有工具方法
    // ============================================================

    /**
     * 鉴权检查：记录存在 且 属于当前用户
     */
    private void checkOwnership(Recognition recognition, Long recognitionId, Long userId) {
        if (recognition == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "识别记录不存在");
        }
        if (!recognition.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看此识别记录");
        }
    }

    /**
     * 实体转 VO（JSON 数组字段反序列化为 List）
     */
    private RecognitionVO convertToVO(Recognition recognition) {
        RecognitionVO vo = new RecognitionVO();
        vo.setId(recognition.getId());
        vo.setStatus(recognition.getStatus());
        vo.setSubmissionId(recognition.getSubmissionId());
        vo.setJobId(recognition.getJobId());
        vo.setRa(recognition.getRa());
        vo.setDec(recognition.getDec());
        vo.setOrientation(recognition.getOrientation());
        vo.setRadius(recognition.getRadius());
        vo.setResultImageUrl(recognition.getResultImageUrl());
        vo.setFailReason(recognition.getFailReason());
        vo.setCreateTime(recognition.getCreateTime());

        // 解析 JSON 数组字段
        vo.setObjectsInField(parseJsonArray(recognition.getObjectsInField(), "objectsInField"));
        vo.setMachineTags(parseJsonArray(recognition.getMachineTags(), "machineTags"));

        if (recognition.getRecommendedProducts() != null) {
            try {
                List<Long> ids = JSON.parseArray(recognition.getRecommendedProducts(), Long.class);
                vo.setRecommendedProductIds(ids);
            } catch (Exception e) {
                log.warn("[Recognition] 解析 recommendedProducts 失败");
            }
        }

        return vo;
    }

    /** 安全解析 JSON 字符串数组 */
    private List<String> parseJsonArray(String jsonStr, String fieldName) {
        if (jsonStr == null) return null;
        try {
            return JSON.parseArray(jsonStr, String.class);
        } catch (Exception e) {
            log.warn("[Recognition] 解析 {} 失败: {}", fieldName, jsonStr);
            return null;
        }
    }
}
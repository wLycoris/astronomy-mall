package com.astronomy.mall.module.recognition.service;

import com.astronomy.mall.module.recognition.vo.RecognitionProductVO;

import java.util.List;

/**
 * AI星图识别 - 器材推荐 Service 接口
 *
 * 📌 核心逻辑:
 *   tb_recognition.machine_tags → TAG_MAPPING → tb_product.tags → 最多6个推荐商品
 *   推荐结果 ID 列表同步写回 tb_recognition.recommended_products (JSON)
 *   无匹配时兜底返回热销商品前6个
 *
 * @since 4.4 (2026-03-16)
 */
public interface RecognitionRecommendService {

    /**
     * 获取推荐器材列表
     *
     * @param recognitionId 识别记录ID
     * @param userId        当前用户ID（鉴权）
     * @return 推荐商品列表，最多6个
     */
    List<RecognitionProductVO> getRecommend(Long recognitionId, Long userId);
}
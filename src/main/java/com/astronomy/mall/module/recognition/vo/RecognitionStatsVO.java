package com.astronomy.mall.module.recognition.vo;

import lombok.Data;

/**
 * 识别统计 VO
 *
 * 路径: com.astronomy.mall.module.recognition.vo.RecognitionStatsVO
 *
 * 对应接口: GET /api/recognition/stats
 */
@Data
public class RecognitionStatsVO {

    /** 总识别次数 */
    private Integer total;

    /** 识别成功次数（status = 1） */
    private Integer successCount;

    /** 识别失败次数（status = 2） */
    private Integer failCount;

    /** 识别中次数（status = 0） */
    private Integer pendingCount;

    /**
     * 成功率（百分比，保留1位小数，如 75.0）
     * total = 0 时返回 0.0
     */
    private Double successRate;
}
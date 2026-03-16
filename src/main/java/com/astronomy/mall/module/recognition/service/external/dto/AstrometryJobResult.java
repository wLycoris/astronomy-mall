package com.astronomy.mall.module.recognition.service.external.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Astrometry.net Job 结果 DTO
 *
 * 用于在 AstrometryServiceImpl 内部传递 calibration 和 job info 数据。
 * 同时承载两种 API 的返回值，部分字段在各自场景下有值：
 *
 * getJobCalibration() 填充:
 *   - ra, dec, orientation, radius, pixscale
 *
 * getJobInfo() 填充:
 *   - objectsInField, machineTags
 */
@Data
public class AstrometryJobResult {

    // ============================================================
    // calibration 字段（/api/jobs/{jobId}/calibration）
    // ============================================================

    /** 赤经（Right Ascension，度，范围 0~360） */
    private BigDecimal ra;

    /** 赤纬（Declination，度，范围 -90~+90） */
    private BigDecimal dec;

    /** 方向角（度） */
    private BigDecimal orientation;

    /** 视野半径（度） */
    private BigDecimal radius;

    /** 像素比例尺（角秒/像素，调试用，不存库） */
    private BigDecimal pixscale;

    // ============================================================
    // job info 字段（/api/jobs/{jobId}/info）
    // ============================================================

    /**
     * 识别到的天体名称列表
     * 示例: ["Orion Nebula", "M42", "NGC 1976"]
     */
    private List<String> objectsInField;

    /**
     * 机器标签列表（天体类型标签）
     * 示例: ["nebula", "emission", "hii", "reflection"]
     */
    private List<String> machineTags;
}
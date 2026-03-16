package com.astronomy.mall.module.recognition.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI星图识别结果 VO
 *
 * 用于向前端返回识别记录数据。
 * 📌 不包含 imageData 字段（大字段，前端本地已有）
 *
 * 状态码说明:
 *   0 - 识别中（等待 Astrometry.net 处理）
 *   1 - 识别成功（可展示坐标/天体/标注图）
 *   2 - 识别失败（展示 failReason）
 *
 * 前端根据 status 决定页面展示逻辑:
 *   - status=0 → 继续轮询（4.2节实现）
 *   - status=1 → 跳转识别结果页
 *   - status=2 → 展示失败原因，提供重新上传按钮
 */
@Data
public class RecognitionVO {

    /** 识别记录 ID（即 recognitionId，前端用于轮询） */
    private Long id;

    /**
     * 识别状态
     * 0-识别中  1-成功  2-失败
     */
    private Integer status;

    /** Astrometry submission ID（调试用，前端通常不直接展示） */
    private String submissionId;

    /** Astrometry job ID（调试用） */
    private String jobId;

    /**
     * 识别到的天体列表
     * 示例: ["Orion Nebula", "M42", "NGC 1976"]
     */
    private List<String> objectsInField;

    /**
     * 机器标签列表
     * 示例: ["nebula", "emission", "reflection"]
     */
    private List<String> machineTags;

    /** 赤经（度） */
    private BigDecimal ra;

    /** 赤纬（度） */
    private BigDecimal dec;

    /** 方向角（度） */
    private BigDecimal orientation;

    /** 视野半径（度） */
    private BigDecimal radius;

    /**
     * Astrometry.net 标注图片 URL
     * ⚠️ 前端直接用 img src 展示，无需额外处理
     * 后端下载此 URL 时需要 Referer 请求头（AstrometryService 统一处理）
     */
    private String resultImageUrl;

    /**
     * 推荐商品 ID 列表（由推荐模块填充，4.3节）
     */
    private List<Long> recommendedProductIds;

    /** 失败原因（status=2 时有值） */
    private String failReason;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
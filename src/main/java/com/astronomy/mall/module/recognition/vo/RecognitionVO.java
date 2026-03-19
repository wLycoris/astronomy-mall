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
 * v4.3 新增:
 *   - celestialObjects: 天体中英文对照列表 (GET /result/{id} 使用)
 *   - raFormatted / decFormatted / orientationFormatted / radiusFormatted: 坐标格式化字符串
 *
 * v4.5 新增:
 *   - hasImage   : 是否有原始上传图片（历史列表使用）
 *   - mainObjects: 主要天体中文名列表（历史列表使用）
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
     * 识别到的天体列表（英文原始名称）
     * 示例: ["Orion Nebula", "M42", "NGC 1976"]
     */
    private List<String> objectsInField;

    /**
     * 天体中英文对照列表
     * 📌 v4.3新增 - GET /recognition/result/{id} 接口返回
     * 格式: [{"en": "Orion Nebula", "zh": "猎户座大星云", "type": "nebula"}]
     * 无中文名时 zh == en
     */
    private List<CelestialObjectVO> celestialObjects;

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

    // =============================================
    // v4.3 新增：坐标格式化字符串
    // =============================================

    /**
     * 赤经格式化字符串
     * 📌 v4.3新增 - 度数转时分秒，如 "05h 35m 17.3s"
     */
    private String raFormatted;

    /**
     * 赤纬格式化字符串
     * 📌 v4.3新增 - 度数转度分秒，如 "-05° 23' 28.0\""
     */
    private String decFormatted;

    /**
     * 方向角格式化字符串
     * 📌 v4.3新增 - 如 "178.50°"
     */
    private String orientationFormatted;

    /**
     * 视野半径格式化字符串
     * 📌 v4.3新增 - ≥1° 显示度数；<1° 转为角分，如 "1.23°" 或 "27.0'"
     */
    private String radiusFormatted;

    /**
     * Astrometry.net 标注图片 URL
     * ⚠️ 前端直接用 img src 展示，无需额外处理
     */
    private String resultImageUrl;

    /**
     * 推荐商品 ID 列表（由推荐模块填充，4.4节）
     */
    private List<Long> recommendedProductIds;

    /** 失败原因（status=2 时有值） */
    private String failReason;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // =============================================
    // v4.5 新增：历史列表专用字段
    // =============================================

    /**
     * 是否有原始上传图片
     * 📌 v4.5新增 - 历史列表接口附加，前端据此显示缩略图占位图标
     * true = imageData 字段非空
     */
    private Boolean hasImage;

    /**
     * 主要天体中文名列表
     * 📌 v4.5新增 - 历史列表接口附加，来自 objectsInField 经 CELESTIAL_NAME_MAP 映射
     * 示例: ["猎户座大星云", "猎户座"]
     */
    private List<String> mainObjects;

    // =============================================
    // v4.3 内部类：天体中英文对照
    // =============================================

    /**
     * 天体中英文对照 VO
     * 📌 v4.3新增 - 用于前端按类型渲染不同颜色的 Tag 标签
     */
    @Data
    public static class CelestialObjectVO {

        /** 英文名称（Astrometry 原始返回） */
        private String en;

        /**
         * 中文名称（后端静态 Map 映射）
         * 无匹配时与 en 相同
         */
        private String zh;

        /**
         * 天体类型（用于前端 Tag 颜色区分）
         * 取值: nebula / galaxy / cluster / constellation / unknown
         */
        private String type;

        public CelestialObjectVO(String en, String zh, String type) {
            this.en   = en;
            this.zh   = zh;
            this.type = type;
        }
    }
}
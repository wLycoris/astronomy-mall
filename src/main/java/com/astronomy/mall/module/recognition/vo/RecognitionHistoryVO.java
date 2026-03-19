package com.astronomy.mall.module.recognition.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 识别历史列表项 VO（轻量级）
 *
 * 路径: com.astronomy.mall.module.recognition.vo.RecognitionHistoryVO
 *
 * 用途: GET /api/recognition/history 历史列表接口返回，
 *       刻意不包含 image_data（base64大字段），避免列表接口数据量过大。
 *       前端用 hasImage 字段决定是否显示占位图标。
 *
 * 字段说明:
 *   - id              : 识别记录ID
 *   - status          : 0-识别中 1-成功 2-失败
 *   - mainObjects     : 主要天体名列表（中文优先，最多取2个，用于列表展示）
 *   - objectsInField  : 原始天体JSON数组（备用）
 *   - hasImage        : 是否有原始图片（image_data IS NOT NULL AND image_data != ''）
 *   - imageThumb      : 图片缩略标志（base64前50字符，供前端判断图片格式，不作为完整图片渲染）
 *   - failReason      : 失败原因（status=2时有值）
 *   - createTime      : 识别提交时间
 */
@Data
public class RecognitionHistoryVO {

    /**
     * 识别记录ID
     */
    private Long id;

    /**
     * 识别状态: 0-识别中  1-成功  2-失败
     */
    private Integer status;

    /**
     * 主要天体名（中文优先，最多2个，来自 objects_in_field 字段解析后中英对照映射）
     * 示例: ["猎户座大星云", "猎户座"]
     */
    private List<String> mainObjects;

    /**
     * 是否有原始上传图片
     * true = image_data 字段非空，前端可显示图片占位图标
     */
    private Boolean hasImage;

    /**
     * 图片缩略标志（imageData 的前50字符，纯用于前端区分图片类型，不作完整图片渲染）
     * 前端展示: 有值时显示星空占位图，无值时显示默认图标
     */
    private String imageThumb;

    /**
     * 失败原因（status=2时有值）
     */
    private String failReason;

    /**
     * 识别提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
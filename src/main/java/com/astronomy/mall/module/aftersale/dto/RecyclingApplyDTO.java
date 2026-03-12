package com.astronomy.mall.module.aftersale.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 二手回收申请 DTO（用户提交）
 *
 * 📌 接口: POST /api/recycling/submit
 */
@Data
public class RecyclingApplyDTO {

    /**
     * 器材名称（必填，不超过200字）
     */
    @NotBlank(message = "器材名称不能为空")
    @Size(max = 200, message = "器材名称不超过200字")
    private String productName;

    /**
     * 品牌（可选，不超过100字）
     */
    @Size(max = 100, message = "品牌不超过100字")
    private String brand;

    /**
     * 型号（可选，不超过100字）
     */
    @Size(max = 100, message = "型号不超过100字")
    private String model;

    /**
     * 成色等级（必填）
     * S - 全新/几乎未使用
     * A - 九成新，无明显磨损
     * B - 七八成新，有轻微使用痕迹
     * C - 六成以下，有明显使用痕迹或瑕疵
     */
    @NotBlank(message = "成色等级不能为空")
    @Pattern(regexp = "^[SABC]$", message = "成色等级只能是 S/A/B/C")
    private String conditionLevel;

    /**
     * 器材描述（可选，不超过1000字）
     */
    @Size(max = 1000, message = "描述不超过1000字")
    private String description;
    /**
     * 器材图片列表（base64 JSON字符串，可为空）
     * 前端序列化：JSON.stringify(base64Array)
     * 示例：["data:image/jpeg;base64,...", ...]
     */
    private String images;
}
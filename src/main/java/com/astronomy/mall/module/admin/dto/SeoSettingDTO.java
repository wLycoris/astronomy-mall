package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 更新 SEO 设置请求 DTO
 * 接口: PUT /api/admin/setting/seo
 */
@Data
public class SeoSettingDTO {

    /** 网站标题，必填 */
    @NotBlank(message = "网站标题不能为空")
    private String seoTitle;

    /** 网站关键词(逗号分隔) */
    private String seoKeywords;

    /** 网站描述 */
    private String seoDescription;
}
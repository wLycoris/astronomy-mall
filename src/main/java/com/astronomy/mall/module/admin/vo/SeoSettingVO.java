package com.astronomy.mall.module.admin.vo;

import lombok.Data;

/**
 * SEO 设置响应 VO
 * 接口: GET /api/admin/setting/seo
 */
@Data
public class SeoSettingVO {

    /** 网站标题 */
    private String seoTitle;

    /** 网站关键词(逗号分隔) */
    private String seoKeywords;

    /** 网站描述 */
    private String seoDescription;
}
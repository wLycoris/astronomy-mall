package com.astronomy.mall.module.admin.vo;

import lombok.Data;

/**
 * 基础设置响应 VO
 * 接口: GET /api/admin/setting/basic
 */
@Data
public class BasicSettingVO {

    /** 商城名称 */
    private String mallName;

    /** 商城 Logo URL */
    private String mallLogo;

    /** 商城简介 */
    private String mallDesc;

    /** 客服电话 */
    private String contactPhone;

    /** 客服邮箱 */
    private String contactEmail;

    /** 客服QQ */
    private String contactQq;

    /** ICP备案号 */
    private String icpNumber;

    /** 版权信息 */
    private String copyright;
}
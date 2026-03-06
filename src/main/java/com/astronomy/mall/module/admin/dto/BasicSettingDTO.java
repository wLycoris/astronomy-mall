package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 更新基础设置请求 DTO
 * 接口: PUT /api/admin/setting/basic
 */
@Data
public class BasicSettingDTO {

    /** 商城名称，必填 */
    @NotBlank(message = "商城名称不能为空")
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
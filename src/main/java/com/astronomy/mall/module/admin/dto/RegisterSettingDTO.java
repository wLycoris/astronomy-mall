package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 更新注册设置请求 DTO
 * 接口: PUT /api/admin/setting/register
 */
@Data
public class RegisterSettingDTO {

    /** 是否开放用户注册，必填 */
    @NotNull(message = "注册开关不能为空")
    private Boolean registerEnabled;

    /** 是否开启邮箱验证 */
    private Boolean emailVerifyEnabled;

    /** 是否仅限邀请注册 */
    private Boolean inviteOnly;

    /** 新用户默认头像 URL */
    private String defaultAvatar;
}
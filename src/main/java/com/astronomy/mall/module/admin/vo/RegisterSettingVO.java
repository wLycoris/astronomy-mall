package com.astronomy.mall.module.admin.vo;

import lombok.Data;

/**
 * 注册设置响应 VO
 * 接口: GET /api/admin/setting/register
 */
@Data
public class RegisterSettingVO {

    /** 是否开放注册 */
    private Boolean registerEnabled;

    /** 是否开启邮箱验证 */
    private Boolean emailVerifyEnabled;

    /** 是否仅限邀请注册 */
    private Boolean inviteOnly;

    /** 新用户默认头像 URL */
    private String defaultAvatar;
}
package com.astronomy.mall.module.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码 DTO
 * 路径: com.astronomy.mall.module.user.dto.ChangePasswordDTO
 *
 * 接口: POST /api/user/change-password
 * 校验规则:
 *   1. 旧密码不能为空
 *   2. 新密码长度 6-20 位
 *   3. 两次新密码必须一致 (在 Service 层校验，不在 DTO 层)
 */
@Data
public class ChangePasswordDTO {

    /**
     * 旧密码 (明文，由 Service 层 MD5 后与数据库比对)
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码 (明文，6-20 位)
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度须在 6-20 位之间")
    private String newPassword;

    /**
     * 确认新密码 (与 newPassword 保持一致，Service 层校验)
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
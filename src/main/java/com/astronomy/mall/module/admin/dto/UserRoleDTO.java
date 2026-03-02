package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 设置用户角色 DTO
 *
 * <p>管理员升级/降级用户角色时的入参。</p>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
@Data
public class UserRoleDTO {

    /**
     * 目标角色（不能为空）
     * <ul>
     *   <li>0 - 普通用户</li>
     *   <li>1 - 管理员</li>
     * </ul>
     */
    @NotNull(message = "角色不能为空")
    private Integer role;
}
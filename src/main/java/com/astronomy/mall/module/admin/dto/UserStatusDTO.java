package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 修改用户状态 DTO
 *
 * <p>管理员禁用或启用用户时的入参，支持填写操作原因。</p>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
@Data
public class UserStatusDTO {

    /**
     * 目标状态（不能为空）
     * <ul>
     *   <li>0 - 禁用</li>
     *   <li>1 - 启用</li>
     * </ul>
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 操作原因（选填）
     * <p>禁用时建议填写原因，便于后期审计。</p>
     */
    private String reason;
}
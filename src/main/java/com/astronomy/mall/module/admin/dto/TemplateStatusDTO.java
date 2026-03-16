package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 通知模板启用/禁用 DTO
 * 用于 POST /api/admin/notification/template/status 接口
 */
@Data
public class TemplateStatusDTO {

    /** 模板ID（必填） */
    @NotNull(message = "模板ID不能为空")
    private Long id;

    /** 目标状态（必填，0-禁用 1-启用） */
    @NotNull(message = "状态不能为空")
    private Integer enabled;
}
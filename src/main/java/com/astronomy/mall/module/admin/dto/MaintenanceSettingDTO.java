package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 更新维护模式请求 DTO
 * 接口: PUT /api/admin/setting/maintenance
 */
@Data
public class MaintenanceSettingDTO {

    /** 是否开启维护模式，必填 */
    @NotNull(message = "维护模式开关不能为空")
    private Boolean maintenanceMode;

    /** 维护提示语 */
    private String maintenanceMessage;

    /** 预计恢复时间（可选，字符串格式，如 "2026-03-05 08:00"） */
    private String maintenanceEndTime;
}
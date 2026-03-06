package com.astronomy.mall.module.admin.vo;

import lombok.Data;

/**
 * 维护模式响应 VO
 * 接口: GET /api/admin/setting/maintenance
 */
@Data
public class MaintenanceSettingVO {

    /** 是否处于维护模式 */
    private Boolean maintenanceMode;

    /** 维护提示语 */
    private String maintenanceMessage;

    /** 预计恢复时间 */
    private String maintenanceEndTime;
}
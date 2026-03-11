package com.astronomy.mall.module.aftersale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 器材保养提醒实体类
 * 对应数据库表: tb_service_reminder
 *
 * ⚠️ 命名说明: 使用 ServiceReminder 前缀，避免与 MaintenanceSettingDTO（系统维护模式 DTO）发生命名冲突
 *
 * 📌 关联关系:
 *   user_id → tb_user.id (用户)
 *
 * 📌 提醒类型 (remind_type):
 *   clean    - 光学清洁
 *   calibrate - 赤道仪校准
 *   check    - 常规检查
 *   custom   - 自定义
 */
@Data
@TableName("tb_service_reminder")
public class ServiceReminder {

    /**
     * 提醒ID，自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联 tb_user.id
     */
    private Long userId;

    /**
     * 器材名称（用户自填，不强制关联商品表）
     */
    private String productName;

    /**
     * 保养类型
     * clean=光学清洁 / calibrate=校准 / check=常规检查 / custom=自定义
     */
    private String remindType;

    /**
     * 提醒标题
     */
    private String remindTitle;

    /**
     * 提醒日期
     */
    private LocalDate remindDate;

    /**
     * 是否已完成：0=否，1=是
     */
    private Integer isDone;

    /**
     * 完成时间
     */
    private LocalDateTime doneTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
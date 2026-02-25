package com.astronomy.mall.module.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户通知设置实体类
 */
@Data
@TableName("tb_user_notification_setting")
public class UserNotificationSetting {

    /**
     * 设置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模块名称
     */
    @TableField("module")
    private String module;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 是否接收该类型通知 (0-关闭 1-开启)
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
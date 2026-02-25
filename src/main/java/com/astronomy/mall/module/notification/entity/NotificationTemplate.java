package com.astronomy.mall.module.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知模板实体类
 */
@Data
@TableName("tb_notification_template")
public class NotificationTemplate {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码 (唯一)
     */
    private String code;

    /**
     * 所属模块
     */
    @TableField("module")
    private String module;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 标题模板 (支持占位符)
     */
    private String titleTemplate;

    /**
     * 内容模板 (支持占位符)
     */
    private String contentTemplate;

    /**
     * 跳转URL模板
     */
    private String jumpUrlTemplate;

    /**
     * 变量说明 (JSON格式)
     */
    private String variables;

    /**
     * 是否启用 (0-禁用 1-启用)
     */
    private Integer enabled;

    /**
     * 备注说明
     */
    private String remark;

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
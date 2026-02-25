package com.astronomy.mall.module.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
@TableName("tb_notification")
public class Notification {

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 所属模块 (mall/forum/course/location/recommend/ai/system)
     */
    @TableField("module")
    private String module;

    /**
     * 通知类型 (order_paid/order_shipped/post_liked等)
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务ID (如订单ID/帖子ID)
     */
    private Long relatedId;

    /**
     * 关联类型 (order/post/course等)
     */
    private String relatedType;

    /**
     * 跳转链接
     */
    private String jumpUrl;

    /**
     * 是否已读 (0-未读 1-已读)
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 优先级 (0-普通 1-重要 2-紧急)
     */
    private Integer priority;

    /**
     * 扩展数据 (JSON格式)
     */
    private String extraData;

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
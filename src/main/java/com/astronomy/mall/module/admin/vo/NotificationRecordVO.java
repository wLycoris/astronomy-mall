package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台通知记录展示 VO
 * 对应 GET /api/admin/notification/record/list 响应体
 *
 * 📌 冗余了用户信息（username/nickname），方便管理端直接展示
 * 📌 moduleLabel / priorityLabel 为前端展示友好的中文标签
 */
@Data
public class NotificationRecordVO {

    /** 通知ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名（来自 tb_user.username） */
    private String username;

    /** 昵称（来自 tb_user.nickname） */
    private String nickname;

    /** 所属模块（mall/system/forum/course/location/recommend/ai） */
    private String module;

    /** 模块中文标签（前端展示用） */
    private String moduleLabel;

    /** 通知类型（order_paid/announcement 等） */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容（可能较长，前端截断展示） */
    private String content;

    /** 关联业务ID（如订单ID/公告ID） */
    private Long relatedId;

    /** 关联业务类型（order/product/announcement 等） */
    private String relatedType;

    /** 跳转链接 */
    private String jumpUrl;

    /** 是否已读（0-未读 1-已读） */
    private Integer isRead;

    /** 阅读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /** 优先级（0-普通 1-重要 2-紧急） */
    private Integer priority;

    /** 优先级中文标签（前端展示用） */
    private String priorityLabel;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
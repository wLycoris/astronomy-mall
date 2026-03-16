package com.astronomy.mall.module.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台通知记录查询 DTO
 * 用于 GET /api/admin/notification/record/list 接口参数封装
 *
 * 📌 字段说明:
 *  - userId   : 按用户筛选（选填）
 *  - module   : 按模块筛选（mall/system/forum/course 等，选填）
 *  - type     : 按通知类型筛选（order_paid 等，选填）
 *  - isRead   : 按已读状态筛选（0-未读 1-已读，选填）
 *  - keyword  : 标题/内容关键词模糊搜索（选填）
 *  - startTime: 创建时间起始（选填）
 *  - endTime  : 创建时间结束（选填）
 */
@Data
public class NotificationRecordQueryDTO {

    /** 当前页，默认第1页 */
    private Integer pageNum = 1;

    /** 每页条数，默认20条 */
    private Integer pageSize = 20;

    /** 用户ID（选填，精确匹配） */
    private Long userId;

    /** 所属模块（选填，如 mall/system/forum/course） */
    private String module;

    /** 通知类型（选填，如 order_paid/announcement） */
    private String type;

    /** 是否已读（选填，0-未读 1-已读） */
    private Integer isRead;

    /** 关键词（选填，模糊匹配标题和内容） */
    private String keyword;

    /** 优先级（选填，0-普通 1-重要 2-紧急） */
    private Integer priority;

    /** 创建时间 - 起始（选填） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 创建时间 - 结束（选填） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
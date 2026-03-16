package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知模板展示 VO
 * 对应 GET /api/admin/notification/template/list 和 GET /api/admin/notification/template/{id} 响应体
 *
 * 📌 与 tb_notification_template 一一对应
 * 📌 额外提供 moduleLabel / 变量示例，方便前端展示
 */
@Data
public class NotificationTemplateVO {

    /** 模板ID */
    private Long id;

    /** 模板唯一编码（如 MALL_ORDER_PAID） */
    private String code;

    /** 所属模块（mall/system/forum/course 等） */
    private String module;

    /** 模块中文标签（前端分组展示用） */
    private String moduleLabel;

    /** 通知类型（order_paid/announcement 等） */
    private String type;

    /** 标题模板（含 {变量} 占位符） */
    private String titleTemplate;

    /** 内容模板（含 {变量} 占位符） */
    private String contentTemplate;

    /** 跳转链接模板（含 {变量} 占位符，可为null） */
    private String jumpUrlTemplate;

    /** 变量说明（JSON字符串，如 {"orderNo":"订单号","amount":"金额"}） */
    private String variables;

    /** 是否启用（0-禁用 1-启用） */
    private Integer enabled;

    /** 备注说明 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
package com.astronomy.mall.module.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通知记录统计分析 VO
 * 对应 GET /api/admin/notification/record/stats 响应体
 *
 * 包含:
 *  - 总量、已读/未读汇总数据
 *  - 按模块分布（用于饼图）
 *  - 近30天每日发送量（用于柱状图）
 *  - 按通知类型 Top10 分布
 */
@Data
public class NotificationStatsVO {

    /** 通知总数（未删除） */
    private Long totalCount;

    /** 未读数量 */
    private Long unreadCount;

    /** 已读数量 */
    private Long readCount;

    /** 今日发送数量 */
    private Long todayCount;

    /** 本月发送数量 */
    private Long monthCount;

    /** 按模块分布（用于饼图） */
    private List<ModuleStatItem> moduleStats;

    /** 近30天每日发送量（用于柱状图） */
    private List<DateStatItem> dateStats;

    /** 按通知类型 Top10 分布 */
    private List<TypeStatItem> typeStats;

    // ==================== 内部 Item 类 ====================

    /** 模块分布统计项 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleStatItem {
        /** 模块代码（mall/system/forum 等） */
        private String module;
        /** 模块中文名称 */
        private String moduleLabel;
        /** 该模块通知数量 */
        private Long count;
        /** 占比百分比（保留1位小数，如 35.2） */
        private Double percentage;
    }

    /** 按日期统计项 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateStatItem {
        /** 日期（格式：MM-dd，如 03-16） */
        private String date;
        /** 当天发送数量 */
        private Long count;
    }

    /** 通知类型统计项 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStatItem {
        /** 通知类型代码（order_paid 等） */
        private String type;
        /** 类型中文说明 */
        private String typeLabel;
        /** 该类型通知数量 */
        private Long count;
    }
}
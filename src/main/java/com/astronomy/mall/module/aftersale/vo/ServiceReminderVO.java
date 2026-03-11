package com.astronomy.mall.module.aftersale.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 器材保养提醒 VO（返回给前端的视图对象）
 *
 * 📌 前端根据 remindDate 自行计算 daysUntil，不依赖后端推送：
 *   diff = dayjs(remindDate).diff(dayjs(), 'day')
 *   diff <= 7 && !isDone → 标红"还有X天"
 *   diff < 0 && !isDone  → 显示"已逾期"
 *
 * ⚠️ 日期字段加 @JsonFormat，保证序列化格式一致（前端 dayjs 能正确解析）
 *
 * 路径: com.astronomy.mall.module.aftersale.vo.ServiceReminderVO
 */
@Data
public class ServiceReminderVO {

    /** 提醒ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 器材名称 */
    private String productName;

    /**
     * 保养类型
     * clean=光学清洁 / calibrate=校准 / check=常规检查 / custom=自定义
     */
    private String remindType;

    /**
     * 保养类型中文标签（后端转换，方便前端直接展示）
     */
    private String remindTypeLabel;

    /** 提醒标题 */
    private String remindTitle;

    /**
     * 提醒日期
     * 前端通过此字段计算剩余天数并决定显示颜色
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate remindDate;

    /** 是否已完成：0=否，1=是 */
    private Integer isDone;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime doneTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
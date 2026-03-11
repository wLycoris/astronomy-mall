package com.astronomy.mall.module.aftersale.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 器材保养提醒 DTO（新增 / 编辑 / 标记完成 复用）
 *
 * ⚠️ 避坑提醒: LocalDate 字段务必加 @JsonFormat，
 *    否则 Jackson 无法正确反序列化前端传来的 "yyyy-MM-dd" 格式日期字符串
 *    （参考 2.5.1 安装预约模块踩坑记录）
 *
 * 📌 新增时：productName / remindTitle / remindDate 必传，id 不传
 * 📌 编辑时：id + 需要修改的字段
 * 📌 标记完成：isDone=1，可附带 remindDate（下次提醒日期，前端将其视为"续期"）
 *
 * 路径: com.astronomy.mall.module.aftersale.dto.ServiceReminderDTO
 */
@Data
public class ServiceReminderDTO {

    /**
     * 提醒ID（编辑/标记完成时传入，新增不传）
     */
    private Long id;

    /**
     * 器材名称，新增/编辑时必填
     */
    @NotBlank(message = "器材名称不能为空")
    private String productName;

    /**
     * 保养类型
     * 可选值: clean / calibrate / check / custom（默认 custom）
     */
    private String remindType;

    /**
     * 提醒标题，新增/编辑时必填
     */
    @NotBlank(message = "提醒标题不能为空")
    private String remindTitle;

    /**
     * 提醒日期（新增/编辑时必填；标记完成后选择"下次提醒"时也传）
     *
     * ⚠️ 必须加 @JsonFormat，否则 Spring Boot 无法解析 "yyyy-MM-dd"
     */
    @NotNull(message = "提醒日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate remindDate;

    /**
     * 是否已完成：0=否，1=是
     * 标记完成时传 1，重新激活时传 0（编辑场景）
     */
    private Integer isDone;
}
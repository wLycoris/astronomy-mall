package com.astronomy.mall.module.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 管理员手动批量同步 APOD 历史数据的请求 DTO
 *
 * 📌 对应接口: POST /api/admin/course/apod/sync
 *
 * 📌 使用场景:
 * - 首次部署时补充历史 APOD 数据
 * - 定时任务漏跑时手动补录
 * - 已存在的日期自动跳过（幂等）
 *
 * 📌 使用示例（Apifox/Postman）:
 * {
 *   "startDate": "2024-01-01",
 *   "endDate": "2024-01-31"
 * }
 *
 * ⚠️ 注意:
 * - 日期范围不能超过 90 天（避免 NASA API 超限）
 * - startDate 不能晚于 endDate
 * - NASA APOD 最早日期为 1995-06-16
 */
@Data
public class ApodSyncDTO {

    /**
     * 开始日期（含）
     * 格式: yyyy-MM-dd
     * 例如: "2024-01-01"
     */
    @NotNull(message = "startDate 不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期（含）
     * 格式: yyyy-MM-dd
     * 例如: "2024-01-31"
     */
    @NotNull(message = "endDate 不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
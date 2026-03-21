package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.ApodSyncDTO;
import com.astronomy.mall.module.course.task.APODSyncScheduler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员课程管理 Controller
 *
 * 📌 5.2 当前只开发一个端点:
 *   POST /api/admin/course/apod/sync  批量同步历史 APOD 数据
 *
 * 📌 5.5 管理端完整 11 个接口到时候在这里补充:
 *   POST   /api/admin/course               新增课程
 *   PUT    /api/admin/course/{id}          编辑课程
 *   DELETE /api/admin/course/{id}          删除课程
 *   PUT    /api/admin/course/{id}/status   发布/下架
 *   GET    /api/admin/course/list          课程列表
 *   POST   /api/admin/course/{id}/chapter  新增章节
 *   PUT    /api/admin/course/chapter/{id}  编辑章节
 *   DELETE /api/admin/course/chapter/{id}  删除章节
 *   GET    /api/admin/course/{id}/chapters 章节列表
 *   GET    /api/admin/course/review/list   评价列表
 *   DELETE /api/admin/course/review/{id}   删除评价
 *
 * ⚠️ 5.2 阶段直接注入 APODSyncScheduler，不需要额外的 AdminCourseService 层
 */
@Slf4j
@Api(tags = "管理员 - 课程管理")
@RestController
@RequestMapping("/admin/course")
public class AdminCourseController {

    /**
     * 直接注入 APODSyncScheduler
     * 5.2 只用到 syncApodRange()，不需要额外 Service 包装
     */
    @Autowired
    private APODSyncScheduler apodSyncScheduler;

    /**
     * 管理员手动批量同步历史 APOD 数据
     *
     * POST /api/admin/course/apod/sync
     *
     * Body 示例:
     * {
     *   "startDate": "2024-01-01",
     *   "endDate":   "2024-01-31"
     * }
     *
     * 返回示例:
     * { "code": 200, "data": 28, "message": "批量同步完成，共新增 28 条章节" }
     *
     * 📌 已存在的日期自动跳过（幂等，可重复执行）
     * 📌 每次请求间隔 500ms，避免触发 NASA API 频率限制（每小时 1000 次）
     * ⚠️ 单次日期范围不超过 90 天，超过请分批执行
     */
    @ApiOperation("批量同步历史 APOD 数据")
    @AdminLog("批量同步APOD历史数据")
    @PostMapping("/apod/sync")
    public Result<Integer> batchSyncApod(@Validated @RequestBody ApodSyncDTO dto) {

        // 1. startDate 不能晚于 endDate
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            return Result.error("startDate 不能晚于 endDate");
        }

        // 2. 范围不超过 90 天（防接口被滥用拖死 NASA 额度）
        long daysBetween = dto.getEndDate().toEpochDay() - dto.getStartDate().toEpochDay();
        if (daysBetween > 90) {
            return Result.error("日期范围不能超过 90 天，请分批执行");
        }

        log.info("管理员触发 APOD 批量同步: {} ~ {}", dto.getStartDate(), dto.getEndDate());

        // 3. 同步执行（阻塞，完成后才返回结果）
        int count = apodSyncScheduler.syncApodRange(dto.getStartDate(), dto.getEndDate());

        // ✅ 修复: Result.success(String message, T data) — message 在前，data 在后
        return Result.success("批量同步完成，共新增 " + count + " 条章节", count);
    }
}
package com.astronomy.mall.module.admin.service;

import java.time.LocalDate;

/**
 * 管理员课程管理 Service 接口
 *
 * 📌 当前实现（5.2）: 只包含 APOD 批量同步方法
 * 后续完整 AdminCourseController 开发时在此接口补充其余方法
 */
public interface AdminCourseService {

    /**
     * 批量同步历史 APOD 数据到「NASA每日天文图片精选」课程
     *
     * 📌 由 APODSyncScheduler.syncApodRange() 实际执行
     * 本方法作为 Service 层中转，方便后续扩展（如限流、权限校验、日志）
     *
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 本次实际新增的章节数量
     */
    int batchSyncApod(LocalDate startDate, LocalDate endDate);
}
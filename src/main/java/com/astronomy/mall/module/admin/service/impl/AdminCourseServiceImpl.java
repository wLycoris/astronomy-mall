package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.module.admin.service.AdminCourseService;
import com.astronomy.mall.module.course.task.APODSyncScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 管理员课程管理 ServiceImpl
 *
 * 📌 当前实现（5.2）: 只包含 APOD 批量同步
 * 委托给 APODSyncScheduler.syncApodRange() 执行实际逻辑
 */
@Slf4j
@Service
public class AdminCourseServiceImpl implements AdminCourseService {

    /**
     * 注入 APODSyncScheduler
     * ⚠️ 直接注入 Scheduler Bean（不是 @Async 调用），
     *    syncApodRange() 是同步阻塞执行，等全部完成后才返回给前端
     */
    @Autowired
    private APODSyncScheduler apodSyncScheduler;

    /**
     * 批量同步历史 APOD 数据
     *
     * 📌 执行逻辑委托给 APODSyncScheduler.syncApodRange()
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 实际新增章节数
     */
    @Override
    public int batchSyncApod(LocalDate startDate, LocalDate endDate) {
        log.info("[AdminCourseService] 开始批量同步 APOD: {} ~ {}", startDate, endDate);
        return apodSyncScheduler.syncApodRange(startDate, endDate);
    }
}
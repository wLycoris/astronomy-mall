package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.NotificationRecordQueryDTO;
import com.astronomy.mall.module.admin.service.AdminNotificationService;
import com.astronomy.mall.module.admin.vo.NotificationRecordVO;
import com.astronomy.mall.module.admin.vo.NotificationStatsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台通知记录管理 Controller
 *
 * 接口列表 (3个):
 *   GET    /api/admin/notification/record/list   - 通知记录列表（分页+筛选）
 *   GET    /api/admin/notification/record/stats  - 统计分析
 *   DELETE /api/admin/notification/record/batch  - 批量删除
 *
 * 📌 所有接口需要管理员权限（由 AdminInterceptor 统一拦截 /api/admin/**）
 */
@Api(tags = "后台-通知记录管理")
@RestController
@RequestMapping("/api/admin/notification/record")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    /**
     * 1. 通知记录列表（分页 + 多条件筛选）
     *
     * GET /api/admin/notification/record/list
     *
     * 支持筛选：
     *   - userId   : 按用户筛选
     *   - module   : 按模块筛选（mall/system 等）
     *   - type     : 按通知类型筛选
     *   - isRead   : 已读/未读 (0/1)
     *   - keyword  : 标题/内容关键词
     *   - startTime/endTime: 创建时间范围
     *   - priority : 优先级
     */
    @ApiOperation("通知记录列表（分页+筛选）")
    @GetMapping("/list")
    public Result<IPage<NotificationRecordVO>> getNotificationList(NotificationRecordQueryDTO dto) {
        IPage<NotificationRecordVO> page = adminNotificationService.getNotificationPage(dto);
        return Result.success(page);
    }

    /**
     * 2. 通知记录统计分析
     *
     * GET /api/admin/notification/record/stats
     *
     * 返回：
     *   - totalCount/readCount/unreadCount/todayCount/monthCount（汇总数字）
     *   - moduleStats（按模块分布，饼图用）
     *   - dateStats  （近30天每日数量，柱状图用）
     *   - typeStats  （通知类型 Top10）
     */
    @ApiOperation("通知记录统计分析")
    @GetMapping("/stats")
    public Result<NotificationStatsVO> getNotificationStats() {
        NotificationStatsVO stats = adminNotificationService.getNotificationStats();
        return Result.success(stats);
    }

    /**
     * 3. 批量逻辑删除通知记录
     *
     * DELETE /api/admin/notification/record/batch
     * Body: { "ids": [1, 2, 3] }
     *
     * 注意：使用逻辑删除（deleted=1），不物理删除
     * 单次最多删除500条
     */
    @ApiOperation("批量删除通知记录")
    @AdminLog("批量删除通知记录")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        adminNotificationService.batchDeleteNotifications(ids);
        return Result.success(null);
    }
}
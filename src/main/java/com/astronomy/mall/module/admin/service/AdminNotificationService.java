package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.NotificationRecordQueryDTO;
import com.astronomy.mall.module.admin.vo.NotificationRecordVO;
import com.astronomy.mall.module.admin.vo.NotificationStatsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 后台通知记录管理 Service 接口
 *
 * 📌 职责：
 *   - 分页查询全用户通知记录
 *   - 统计分析（模块分布、按日统计）
 *   - 批量逻辑删除
 */
public interface AdminNotificationService {

    /**
     * 分页查询通知记录（后台视角，跨用户）
     *
     * @param dto 查询条件（userId/module/type/isRead/keyword/time范围等）
     * @return 分页结果
     */
    IPage<NotificationRecordVO> getNotificationPage(NotificationRecordQueryDTO dto);

    /**
     * 通知记录统计分析
     * 返回：总量、已读/未读汇总、模块分布、近30天每日量、类型Top10
     *
     * @return 统计数据
     */
    NotificationStatsVO getNotificationStats();

    /**
     * 批量逻辑删除通知记录（设 deleted = 1）
     *
     * @param ids 要删除的通知ID列表（不能为空）
     */
    void batchDeleteNotifications(List<Long> ids);
}
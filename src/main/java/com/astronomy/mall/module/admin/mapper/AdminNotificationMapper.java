package com.astronomy.mall.module.admin.mapper;

import com.astronomy.mall.module.admin.dto.NotificationRecordQueryDTO;
import com.astronomy.mall.module.admin.vo.NotificationRecordVO;
import com.astronomy.mall.module.admin.vo.NotificationStatsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 后台通知记录 Mapper（管理员视角，跨用户查询）
 *
 * 📌 与 notification 模块的 NotificationMapper 区别：
 *    - NotificationMapper 面向用户端，只查当前用户的通知
 *    - AdminNotificationMapper 面向后台，查所有用户的通知
 *
 * 📌 XML 文件位置：
 *    resources/mapper/AdminNotificationMapper.xml
 */
@Mapper
public interface AdminNotificationMapper {

    /**
     * 分页查询通知记录（JOIN tb_user 获取用户信息）
     *
     * @param page 分页对象
     * @param dto  查询条件
     * @return 分页结果
     */
    IPage<NotificationRecordVO> selectNotificationPage(
            Page<NotificationRecordVO> page,
            @Param("dto") NotificationRecordQueryDTO dto
    );

    /**
     * 查询各模块通知分布（用于饼图）
     *
     * @return [{module, count}] 列表
     */
    List<NotificationStatsVO.ModuleStatItem> selectModuleStats();

    /**
     * 查询近30天每日通知数量（用于柱状图）
     *
     * @return [{date, count}] 列表
     */
    List<NotificationStatsVO.DateStatItem> selectDateStats();

    /**
     * 查询通知类型 Top10 分布
     *
     * @return [{type, count}] 列表，最多10条
     */
    List<NotificationStatsVO.TypeStatItem> selectTypeStats();

    /**
     * 查询汇总数量（总数、已读、未读、今日、本月）
     *
     * @return Map 包含 totalCount/readCount/unreadCount/todayCount/monthCount
     */
    java.util.Map<String, Object> selectCountSummary();

    /**
     * 批量逻辑删除通知（将 deleted 设为 1）
     *
     * @param ids 通知ID列表
     * @return 影响行数
     */
    int batchLogicDelete(@Param("ids") List<Long> ids);
}
package com.astronomy.mall.module.notification.service;

import com.astronomy.mall.module.notification.dto.MarkReadDTO;
import com.astronomy.mall.module.notification.dto.NotificationQueryDTO;
import com.astronomy.mall.module.notification.dto.NotificationSettingDTO;
import com.astronomy.mall.module.notification.dto.SendNotificationDTO;
import com.astronomy.mall.module.notification.vo.NotificationSettingVO;
import com.astronomy.mall.module.notification.vo.NotificationVO;
import com.astronomy.mall.module.notification.vo.UnreadCountVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送通知 (核心方法)
     * @param dto 通知DTO
     */
    void sendNotification(SendNotificationDTO dto);

    /**
     * 批量发送通知
     * @param dtos 通知DTO列表
     */
    void batchSendNotification(List<SendNotificationDTO> dtos);

    /**
     * 分页查询通知列表
     * @param userId 用户ID
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<NotificationVO> getNotificationList(Long userId, NotificationQueryDTO dto);

    /**
     * 获取未读数量统计
     * @param userId 用户ID
     * @return 各模块未读数量
     */
    UnreadCountVO getUnreadCount(Long userId);

    /**
     * 标记为已读
     * @param userId 用户ID
     * @param dto 通知ID列表
     */
    void markAsRead(Long userId, MarkReadDTO dto);

    /**
     * 全部标记为已读
     * @param userId 用户ID
     * @param module 模块(可选,不传则全部标记)
     */
    void markAllAsRead(Long userId, String module);

    /**
     * 删除通知
     * @param userId 用户ID
     * @param id 通知ID
     */
    void deleteNotification(Long userId, Long id);

    /**
     * 获取用户通知设置
     * @param userId 用户ID
     * @return 通知设置列表
     */
    List<NotificationSettingVO> getUserSettings(Long userId);

    /**
     * 更新用户通知设置
     * @param userId 用户ID
     * @param dto 设置DTO
     */
    void updateUserSettings(Long userId, NotificationSettingDTO dto);

    /**
     * 检查用户是否接收该类型通知
     * @param userId 用户ID
     * @param module 模块
     * @param type 类型
     * @return true-接收 false-不接收
     */
    boolean isNotificationEnabled(Long userId, String module, String type);
}
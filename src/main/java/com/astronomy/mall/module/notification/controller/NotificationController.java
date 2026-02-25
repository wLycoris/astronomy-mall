package com.astronomy.mall.module.notification.controller;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.notification.dto.MarkReadDTO;
import com.astronomy.mall.module.notification.dto.NotificationQueryDTO;
import com.astronomy.mall.module.notification.dto.NotificationSettingDTO;
import com.astronomy.mall.module.notification.service.NotificationService;
import com.astronomy.mall.module.notification.vo.NotificationSettingVO;
import com.astronomy.mall.module.notification.vo.NotificationVO;
import com.astronomy.mall.module.notification.vo.UnreadCountVO;
import com.astronomy.mall.utils.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 消息通知控制器
 */
@Api(tags = "消息通知管理")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 分页查询通知列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询通知列表")
    public Result<Page<NotificationVO>> getNotificationList(NotificationQueryDTO dto) {
        Long userId = UserContext.getUserId();
        Page<NotificationVO> page = notificationService.getNotificationList(userId, dto);
        return Result.success(page);
    }

    /**
     * 获取未读数量统计
     */
    @GetMapping("/unread-count")
    @ApiOperation("获取未读数量统计")
    public Result<UnreadCountVO> getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        UnreadCountVO vo = notificationService.getUnreadCount(userId);
        return Result.success(vo);
    }


    /**
     * 标记为已读
     */
    @PostMapping("/mark-read")
    @ApiOperation("标记为已读")
    public Result<Void> markAsRead(@RequestBody @Valid MarkReadDTO dto) {
        Long userId = UserContext.getUserId();
        notificationService.markAsRead(userId, dto);
        return Result.success();
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/mark-all-read")
    @ApiOperation("全部标记为已读")
    public Result<Void> markAllAsRead(@RequestParam(required = false) String module) {
        Long userId = UserContext.getUserId();
        notificationService.markAllAsRead(userId, module);
        return Result.success();
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除通知")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        notificationService.deleteNotification(userId, id);
        return Result.success();
    }

    /**
     * 获取通知设置
     */
    @GetMapping("/settings")
    @ApiOperation("获取通知设置")
    public Result<List<NotificationSettingVO>> getSettings() {
        Long userId = UserContext.getUserId();
        List<NotificationSettingVO> settings = notificationService.getUserSettings(userId);
        return Result.success(settings);
    }

    /**
     * 更新通知设置
     */
    @PostMapping("/settings")
    @ApiOperation("更新通知设置")
    public Result<Void> updateSettings(@RequestBody @Valid NotificationSettingDTO dto) {
        Long userId = UserContext.getUserId();
        notificationService.updateUserSettings(userId, dto);
        return Result.success();
    }
}
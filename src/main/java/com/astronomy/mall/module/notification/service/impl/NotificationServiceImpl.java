package com.astronomy.mall.module.notification.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.notification.dto.MarkReadDTO;
import com.astronomy.mall.module.notification.dto.NotificationQueryDTO;
import com.astronomy.mall.module.notification.dto.NotificationSettingDTO;
import com.astronomy.mall.module.notification.dto.SendNotificationDTO;
import com.astronomy.mall.module.notification.entity.Notification;
import com.astronomy.mall.module.notification.entity.NotificationTemplate;
import com.astronomy.mall.module.notification.entity.UserNotificationSetting;
import com.astronomy.mall.module.notification.enums.NotificationModule;
import com.astronomy.mall.module.notification.enums.NotificationType;
import com.astronomy.mall.module.notification.mapper.NotificationMapper;
import com.astronomy.mall.module.notification.mapper.NotificationTemplateMapper;
import com.astronomy.mall.module.notification.mapper.UserNotificationSettingMapper;
import com.astronomy.mall.module.notification.service.NotificationService;
import com.astronomy.mall.module.notification.vo.NotificationSettingVO;
import com.astronomy.mall.module.notification.vo.NotificationVO;
import com.astronomy.mall.module.notification.vo.UnreadCountVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationTemplateMapper templateMapper;

    @Autowired
    private UserNotificationSettingMapper settingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(SendNotificationDTO dto) {
        log.info("发送通知: userId={}, module={}, type={}", dto.getUserId(), dto.getModule(), dto.getType());

        // 1. 检查用户是否接收该类型通知
        if (!isNotificationEnabled(dto.getUserId(), dto.getModule(), dto.getType())) {
            log.info("用户已关闭该类型通知,跳过发送");
            return;
        }

        // 2. 查询通知模板
        String templateCode = dto.getModule().toUpperCase() + "_" + dto.getType().toUpperCase();
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getCode, templateCode);
        wrapper.eq(NotificationTemplate::getEnabled, 1);
        NotificationTemplate template = templateMapper.selectOne(wrapper);

        if (template == null) {
            log.warn("通知模板不存在或已禁用: {}", templateCode);
            return;
        }

        // 3. 替换模板变量
        String title = replaceVariables(template.getTitleTemplate(), dto.getVariables());
        String content = replaceVariables(template.getContentTemplate(), dto.getVariables());
        String jumpUrl = replaceVariables(template.getJumpUrlTemplate(), dto.getVariables());

        // 4. 创建通知对象
        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setModule(dto.getModule());
        notification.setType(dto.getType());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(dto.getRelatedId());
        notification.setRelatedType(dto.getRelatedType());
        notification.setJumpUrl(jumpUrl);
        notification.setIsRead(0);
        notification.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);

        // 扩展数据转JSON
        if (dto.getExtraData() != null && !dto.getExtraData().isEmpty()) {
            notification.setExtraData(JSONUtil.toJsonStr(dto.getExtraData()));
        }

        // 5. 保存通知
        notificationMapper.insert(notification);

        log.info("通知发送成功: id={}", notification.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSendNotification(List<SendNotificationDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        for (SendNotificationDTO dto : dtos) {
            try {
                sendNotification(dto);
            } catch (Exception e) {
                log.error("批量发送通知失败: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public Page<NotificationVO> getNotificationList(Long userId, NotificationQueryDTO dto) {
        Page<Notification> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);

        // 按模块筛选
        if (StrUtil.isNotBlank(dto.getModule())) {
            wrapper.eq(Notification::getModule, dto.getModule());
        }

        // 按已读状态筛选
        if (dto.getIsRead() != null) {
            wrapper.eq(Notification::getIsRead, dto.getIsRead());
        }

        // 按类型筛选
        if (StrUtil.isNotBlank(dto.getType())) {
            wrapper.eq(Notification::getType, dto.getType());
        }

        // 按时间倒序
        wrapper.orderByDesc(Notification::getCreateTime);

        Page<Notification> notificationPage = notificationMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<NotificationVO> voPage = new Page<>(notificationPage.getCurrent(), notificationPage.getSize(), notificationPage.getTotal());
        List<NotificationVO> voList = new ArrayList<>();

        for (Notification notification : notificationPage.getRecords()) {
            NotificationVO vo = BeanUtil.copyProperties(notification, NotificationVO.class);

            // 设置模块名称和类型名称
            NotificationModule module = NotificationModule.getByCode(notification.getModule());
            if (module != null) {
                vo.setModuleName(module.getName());
            }

            NotificationType type = NotificationType.getByCode(notification.getType());
            if (type != null) {
                vo.setTypeName(type.getName());
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public UnreadCountVO getUnreadCount(Long userId) {

        // 【1】登录校验
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 【2】查询数据库
        List<Map<String, Object>> list =
                notificationMapper.countUnreadByModule(userId);

        // 【3】初始化返回对象（全 0）
        UnreadCountVO vo = UnreadCountVO.builder()
                .total(0L)
                .mall(0L)
                .forum(0L)
                .course(0L)
                .location(0L)
                .recommend(0L)
                .ai(0L)
                .system(0L)
                .build();

        // 【4】没数据直接返回
        if (list == null || list.isEmpty()) {
            return vo;
        }

        // 【5】遍历统计
        long total = 0L;
        for (Map<String, Object> row : list) {
            String module = (String) row.get("module");
            Long count = ((Number) row.get("count")).longValue();

            total += count;

            switch (module) {
                case "mall":
                    vo.setMall(count);
                    break;
                case "forum":
                    vo.setForum(count);
                    break;
                case "course":
                    vo.setCourse(count);
                    break;
                case "location":
                    vo.setLocation(count);
                    break;
                case "recommend":
                    vo.setRecommend(count);
                    break;
                case "ai":
                    vo.setAi(count);
                    break;
                case "system":
                    vo.setSystem(count);
                    break;
            }
        }

        vo.setTotal(total);
        return vo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, MarkReadDTO dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return;
        }

        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.in(Notification::getId, dto.getIds());
        wrapper.set(Notification::getIsRead, 1);
        wrapper.set(Notification::getReadTime, LocalDateTime.now());

        notificationMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId, String module) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId);

        if (StrUtil.isNotBlank(module)) {
            wrapper.eq(Notification::getModule, module);
        }

        wrapper.eq(Notification::getIsRead, 0);
        wrapper.set(Notification::getIsRead, 1);
        wrapper.set(Notification::getReadTime, LocalDateTime.now());

        notificationMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long userId, Long id) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, id);
        wrapper.eq(Notification::getUserId, userId);

        Notification notification = notificationMapper.selectOne(wrapper);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }

        notificationMapper.deleteById(id);
    }

    @Override
    public List<NotificationSettingVO> getUserSettings(Long userId) {
        List<NotificationSettingVO> result = new ArrayList<>();

        // 遍历所有通知类型
        for (NotificationType type : NotificationType.values()) {
            // 查询用户设置
            LambdaQueryWrapper<UserNotificationSetting> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserNotificationSetting::getUserId, userId);
            wrapper.eq(UserNotificationSetting::getModule, type.getModule().getCode());
            wrapper.eq(UserNotificationSetting::getType, type.getCode());
            UserNotificationSetting setting = settingMapper.selectOne(wrapper);

            NotificationSettingVO vo = NotificationSettingVO.builder()
                    .module(type.getModule().getCode())
                    .moduleName(type.getModule().getName())
                    .type(type.getCode())
                    .typeName(type.getName())
                    .enabled(setting != null ? setting.getEnabled() : 1)  // 默认开启
                    .build();

            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserSettings(Long userId, NotificationSettingDTO dto) {
        // 查询是否已存在设置
        LambdaQueryWrapper<UserNotificationSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserNotificationSetting::getUserId, userId);
        queryWrapper.eq(UserNotificationSetting::getModule, dto.getModule());
        queryWrapper.eq(UserNotificationSetting::getType, dto.getType());
        UserNotificationSetting setting = settingMapper.selectOne(queryWrapper);

        if (setting != null) {
            // 更新
            setting.setEnabled(dto.getEnabled());
            settingMapper.updateById(setting);
        } else {
            // 新增
            setting = new UserNotificationSetting();
            setting.setUserId(userId);
            setting.setModule(dto.getModule());
            setting.setType(dto.getType());
            setting.setEnabled(dto.getEnabled());
            settingMapper.insert(setting);
        }
    }

    @Override
    public boolean isNotificationEnabled(Long userId, String module, String type) {
        LambdaQueryWrapper<UserNotificationSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotificationSetting::getUserId, userId);
        wrapper.eq(UserNotificationSetting::getModule, module);
        wrapper.eq(UserNotificationSetting::getType, type);
        UserNotificationSetting setting = settingMapper.selectOne(wrapper);

        // 未设置则默认开启
        return setting == null || setting.getEnabled() == 1;
    }

    @Override
    public Map<String, Object> getAnnouncementDetail(Long relatedId) {
        return notificationMapper.selectAnnouncementByRelatedId(relatedId);
    }

    /**
     * 替换模板变量
     * @param template 模板字符串
     * @param variables 变量Map
     * @return 替换后的字符串
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (StrUtil.isBlank(template)) {
            return "";
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(key, value);
        }

        return result;
    }
}
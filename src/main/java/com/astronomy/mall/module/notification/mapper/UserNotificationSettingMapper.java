package com.astronomy.mall.module.notification.mapper;

import com.astronomy.mall.module.notification.entity.UserNotificationSetting;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知设置Mapper接口
 */
@Mapper
public interface UserNotificationSettingMapper extends BaseMapper<UserNotificationSetting> {
}
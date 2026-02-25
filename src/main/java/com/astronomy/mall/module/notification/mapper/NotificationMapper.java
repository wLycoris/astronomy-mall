package com.astronomy.mall.module.notification.mapper;

import com.astronomy.mall.module.notification.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 统计各模块未读数量
     * @param userId 用户ID
     * @return 每个模块一行数据（module + count）
     */
    List<Map<String, Object>> countUnreadByModule(@Param("userId") Long userId);
}

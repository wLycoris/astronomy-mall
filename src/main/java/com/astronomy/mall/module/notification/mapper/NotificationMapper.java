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

    /**
     * 批量插入通知（用于系统公告批量发送）
     * @param list 通知列表
     */
    void insertBatch(@Param("list") List<Notification> list);

    /**
     * 根据 related_id 查询公告内容（取一条，供用户端公告详情页使用）
     * 不依赖用户ID，任何登录用户都可以查看公告内容
     * @param relatedId 公告分组ID（即 System.currentTimeMillis() 生成的 announcementGroupId）
     * @return 包含 title/content/priority/createTime 的 Map，不存在则返回 null
     */
    Map<String, Object> selectAnnouncementByRelatedId(@Param("relatedId") Long relatedId);
}
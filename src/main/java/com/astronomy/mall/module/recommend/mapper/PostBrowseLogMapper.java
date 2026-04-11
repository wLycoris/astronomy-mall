package com.astronomy.mall.module.recommend.mapper;

import com.astronomy.mall.module.recommend.entity.PostBrowseLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子浏览记录 Mapper
 * 提供帖子推荐所需的用户浏览行为数据
 */
@Mapper
public interface PostBrowseLogMapper extends BaseMapper<PostBrowseLog> {

    /**
     * 查询用户最近浏览的帖子ID列表（去重，按时间倒序）
     * 用于：帖子推荐时获取用户近期感兴趣的帖子
     */
    @Select("SELECT DISTINCT post_id FROM tb_post_browse_log " +
            "WHERE user_id = #{userId} " +
            "ORDER BY browse_time DESC LIMIT #{limit}")
    List<Long> selectRecentPostIds(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 清理90天前的帖子浏览记录
     * 由 RecommendScheduler 每周调用
     */
    @Delete("DELETE FROM tb_post_browse_log WHERE browse_time < DATE_SUB(NOW(), INTERVAL 90 DAY)")
    int deleteExpiredLogs();
}

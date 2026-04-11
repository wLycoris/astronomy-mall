package com.astronomy.mall.module.recommend.mapper;

import com.astronomy.mall.module.recommend.entity.BrowseLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品浏览记录 Mapper
 * 提供 CF 协同过滤所需的用户-商品行为数据查询
 */
@Mapper
public interface BrowseLogMapper extends BaseMapper<BrowseLog> {

    /**
     * 查询用户最近浏览的商品ID列表（去重，按时间倒序）
     * 用于：首页推荐时获取用户近期兴趣商品
     */
    @Select("SELECT DISTINCT product_id FROM tb_browse_log " +
            "WHERE user_id = #{userId} " +
            "ORDER BY browse_time DESC LIMIT #{limit}")
    List<Long> selectRecentProductIds(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询与指定商品有共同浏览行为的用户列表
     * 用于：Item-based CF 计算商品相似度
     */
    @Select("SELECT DISTINCT user_id FROM tb_browse_log WHERE product_id = #{productId}")
    List<Long> selectUserIdsByProductId(@Param("productId") Long productId);

    /**
     * 查询用户浏览过的所有商品及浏览次数
     * 用于：CF 行为矩阵构建（浏览权重=1）
     */
    @Select("SELECT product_id AS productId, COUNT(*) AS cnt FROM tb_browse_log " +
            "WHERE user_id = #{userId} GROUP BY product_id")
    List<Map<String, Object>> selectUserBrowseCounts(@Param("userId") Long userId);

    /**
     * 清理90天前的浏览记录
     * 由 RecommendScheduler 每周调用
     */
    @Delete("DELETE FROM tb_browse_log WHERE browse_time < DATE_SUB(NOW(), INTERVAL 90 DAY)")
    int deleteExpiredLogs();
}

package com.astronomy.mall.module.product.mapper;

import com.astronomy.mall.module.product.entity.ReviewLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评价点赞Mapper接口
 *
 * 功能说明:
 * 1. 管理评价点赞记录
 * 2. 防止重复点赞(通过唯一索引)
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Mapper
public interface ReviewLikeMapper extends BaseMapper<ReviewLike> {

    /**
     * 检查用户是否已点赞
     *
     * 业务规则:
     * - 一个用户对同一条评价只能点赞一次
     * - 通过数据库唯一索引保证
     *
     * 使用场景:
     * - 显示点赞按钮状态(已点赞/未点赞)
     * - 点赞前检查
     *
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 点赞数量(0=未点赞, 1=已点赞)
     */
    @Select("SELECT COUNT(*) FROM tb_review_like " +
            "WHERE review_id = #{reviewId} AND user_id = #{userId}")
    Integer checkUserLiked(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
}
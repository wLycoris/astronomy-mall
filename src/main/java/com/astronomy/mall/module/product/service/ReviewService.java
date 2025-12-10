package com.astronomy.mall.module.product.service;

import com.astronomy.mall.module.product.dto.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 分页获取商品评价列表(基础版 - 保留原有方法)
     *
     * @param productId 商品ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    Page<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize);

    /**
     * 获取评价列表(高级版 - 支持筛选和排序)
     *
     * @param dto 查询条件
     * @param currentUserId 当前用户ID(用于判断是否已点赞)
     * @return 评价列表
     */
    Map<String, Object> getReviewList(ReviewQueryDTO dto, Long currentUserId);

    /**
     * 获取商品评价统计
     *
     * @param productId 商品ID
     * @return 统计信息
     */
    ReviewStatisticsVO getReviewStatistics(Long productId);

    /**
     * 发布评价
     *
     * @param userId 用户ID
     * @param dto 评价信息
     */
    void publishReview(Long userId, PublishReviewDTO dto);

    /**
     * 点赞/取消点赞
     *
     * @param userId 用户ID
     * @param reviewId 评价ID
     */
    void toggleLike(Long userId, Long reviewId);

    /**
     * 商家回复评价
     *
     * @param dto 回复信息
     */
    void replyReview(ReplyReviewDTO dto);

    /**
     * 删除评价
     *
     * @param userId 用户ID
     * @param reviewId 评价ID
     */
    void deleteReview(Long userId, Long reviewId);

    /**
     * 获取用户的评价列表(我的评价)
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    Page<ReviewVO> getUserReviews(Long userId, Integer pageNum, Integer pageSize);
}
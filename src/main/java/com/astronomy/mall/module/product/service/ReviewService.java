package com.astronomy.mall.module.product.service;

import com.astronomy.mall.module.product.dto.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

public interface ReviewService {

    /** 发布评价 */
    void publishReview(Long userId, PublishReviewDTO dto);

    /** 基础版评价列表 */
    Page<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize);

    /** 评价统计 */
    ReviewStatisticsVO getReviewStatistics(Long productId);

    /** 高级版评价列表（筛选+排序） */
    Map<String, Object> getReviewList(ReviewQueryDTO dto, Long currentUserId);

    /** 点赞/取消点赞 */
    void toggleLike(Long userId, Long reviewId);

    /** 商家回复（旧接口兼容） */
    void replyReview(ReplyReviewDTO dto);

    /** 删除评价（用户自己删） */
    void deleteReview(Long userId, Long reviewId);

    /** 我的评价列表（包含已被管理员删除的记录） */
    Page<ReviewVO> getUserReviews(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 举报评价
     * 业务规则：
     * 1. 不能举报自己的评价
     * 2. 每个用户对同一评价只能举报一次
     * 3. 举报次数达到阈值(3次)自动转为待审核(status=2)
     */
    void reportReview(Long userId, Long reviewId, String reason);

    /**
     * 修改评价（直接UPDATE，不走删除重发）
     * 被管理员删除的评价无法修改
     */
    void updateReview(Long userId, Long reviewId, PublishReviewDTO dto);
}
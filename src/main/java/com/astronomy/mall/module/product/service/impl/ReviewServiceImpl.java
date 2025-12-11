package com.astronomy.mall.module.product.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.product.dto.*;
import com.astronomy.mall.module.product.entity.Review;
import com.astronomy.mall.module.product.entity.ReviewLike;
import com.astronomy.mall.module.product.mapper.ReviewLikeMapper;
import com.astronomy.mall.module.product.mapper.ReviewMapper;
import com.astronomy.mall.module.product.service.ReviewService;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价服务实现类(修复版)
 *
 * 修复内容:
 * 1. ✅ checkOrderReviewed 改为检查 订单ID + 商品ID
 * 2. ✅ 一个订单的不同商品可以分别评价
 * 3. ✅ 防止同一商品重复评价
 */
@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private ReviewLikeMapper reviewLikeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrderMapper orderMapper;

    // ============================================
    // 核心修复: 发布评价
    // ============================================

    /**
     * 发布评价(修复版)
     *
     * ✅ 修复内容:
     * 1. 检查改为 订单ID + 商品ID 组合
     * 2. 允许同一订单的不同商品分别评价
     * 3. 防止同一商品重复评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishReview(Long userId, PublishReviewDTO dto) {
        log.info("发布评价: userId={}, dto={}", userId, dto);

        // 1. 检查订单是否存在
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            log.error("订单不存在: orderId={}", dto.getOrderId());
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 检查订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            log.error("订单不属于当前用户: orderId={}, userId={}, orderUserId={}",
                    dto.getOrderId(), userId, order.getUserId());
            throw new BusinessException(ResultCode.REVIEW_NOT_ALLOWED);
        }

        // 3. 检查订单是否已完成(status=3)
        if (order.getStatus() != 3) {
            log.error("订单未完成,无法评价: orderId={}, status={}", dto.getOrderId(), order.getStatus());
            throw new BusinessException(ResultCode.REVIEW_ORDER_NOT_FINISHED);
        }

        // ✅ 4. 检查该商品是否已评价(关键修复点)
        // 改为检查: 订单ID + 商品ID 的组合
        Integer count = reviewMapper.checkProductReviewed(dto.getOrderId(), dto.getProductId(), userId);
        if (count > 0) {
            log.error("该商品已评价: orderId={}, productId={}, userId={}",
                    dto.getOrderId(), dto.getProductId(), userId);
            throw new BusinessException(ResultCode.REVIEW_ALREADY_EXISTS);
        }

        // 5. 保存评价
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(dto.getProductId());
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setIsAnonymous(dto.getIsAnonymous() ? 1 : 0);
        review.setLikeCount(0);
        review.setStatus(1);

        // 6. 处理图片列表
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            if (dto.getImages().size() > 9) {
                log.error("图片数量超过限制: count={}", dto.getImages().size());
                throw new BusinessException(ResultCode.REVIEW_IMAGES_EXCEED_LIMIT);
            }
            review.setImages(JSONUtil.toJsonStr(dto.getImages()));
        }

        // 7. 插入数据库
        reviewMapper.insert(review);
        log.info("评价发布成功: reviewId={}, productId={}", review.getId(), dto.getProductId());
    }

    // ============================================
    // 其他方法保持不变
    // ============================================

    @Override
    public Page<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize) {
        log.info("查询商品评价列表: productId={}, pageNum={}, pageSize={}", productId, pageNum, pageSize);

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
                .eq(Review::getDeleted, 0)
                .orderByDesc(Review::getCreateTime);

        Page<Review> page = new Page<>(pageNum, pageSize);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        Page<ReviewVO> result = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        List<ReviewVO> voList = reviewPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        result.setRecords(voList);

        log.info("查询成功,共{}条评价", result.getTotal());
        return result;
    }

    @Override
    public ReviewStatisticsVO getReviewStatistics(Long productId) {
        log.info("查询商品评价统计: productId={}", productId);

        Map<String, Object> stats = reviewMapper.getReviewStatistics(productId);

        ReviewStatisticsVO vo = new ReviewStatisticsVO();
        vo.setProductId(productId);

        if (stats != null) {
            Object reviewCountObj = stats.get("reviewCount");
            vo.setReviewCount(reviewCountObj == null ? 0 : ((Number) reviewCountObj).intValue());

            Object avgObj = stats.get("avgRating");
            vo.setAvgRating(avgObj == null ? 0.0 : ((Number) avgObj).doubleValue());

            Object goodRateObj = stats.get("goodRate");
            vo.setGoodRate(goodRateObj == null ? 0.0 : ((Number) goodRateObj).doubleValue());

            Object totalLikesObj = stats.get("totalLikes");
            vo.setTotalLikes(totalLikesObj == null ? 0 : ((Number) totalLikesObj).intValue());

            Object fiveStarObj = stats.get("fiveStar");
            vo.setFiveStar(fiveStarObj == null ? 0 : ((Number) fiveStarObj).intValue());

            Object fourStarObj = stats.get("fourStar");
            vo.setFourStar(fourStarObj == null ? 0 : ((Number) fourStarObj).intValue());

            Object threeStarObj = stats.get("threeStar");
            vo.setThreeStar(threeStarObj == null ? 0 : ((Number) threeStarObj).intValue());

            Object twoStarObj = stats.get("twoStar");
            vo.setTwoStar(twoStarObj == null ? 0 : ((Number) twoStarObj).intValue());

            Object oneStarObj = stats.get("oneStar");
            vo.setOneStar(oneStarObj == null ? 0 : ((Number) oneStarObj).intValue());

            Object hasImagesCountObj = stats.get("hasImagesCount");
            vo.setHasImagesCount(hasImagesCountObj == null ? 0 : ((Number) hasImagesCountObj).intValue());
        }

        log.info("统计成功: 总评价={}, 平均分={}, 好评率={}%",
                vo.getReviewCount(), vo.getAvgRating(), vo.getGoodRate());
        return vo;
    }

    @Override
    public Map<String, Object> getReviewList(ReviewQueryDTO dto, Long currentUserId) {
        log.info("查询评价列表(高级版): {}, currentUserId={}", dto, currentUserId);

        int offset = (dto.getPage() - 1) * dto.getPageSize();

        List<Map<String, Object>> reviewList = reviewMapper.getReviewList(
                dto.getProductId(),
                dto.getRating(),
                dto.getHasImages(),
                dto.getSortType(),
                offset,
                dto.getPageSize()
        );

        List<ReviewDetailVO> voList = reviewList.stream().map(map -> {
            ReviewDetailVO vo = new ReviewDetailVO();

            vo.setId(Convert.toLong(map.get("id")));
            vo.setUserId(Convert.toLong(map.get("user_id")));
            vo.setProductId(Convert.toLong(map.get("product_id")));
            vo.setOrderId(Convert.toLong(map.get("order_id")));
            vo.setRating(Convert.toInt(map.get("rating")));
            vo.setContent(Convert.toStr(map.get("content")));
            vo.setLikeCount(Convert.toInt(map.get("like_count")));
            vo.setReplyContent(Convert.toStr(map.get("reply")));
            vo.setIsAnonymous(Convert.toInt(map.get("is_anonymous")) == 1);
            vo.setStatus(Convert.toInt(map.get("status")));
            vo.setProductName(Convert.toStr(map.get("productName")));
            vo.setProductImage(Convert.toStr(map.get("productImage")));

            Object createTime = map.get("create_time");
            if (createTime != null) {
                vo.setCreateTime(createTime.toString());
            }

            Object replyTime = map.get("reply_time");
            if (replyTime != null) {
                vo.setReplyTime(replyTime.toString());
            }

            String imagesJson = Convert.toStr(map.get("images"));
            if (imagesJson != null && !imagesJson.isEmpty()) {
                if (imagesJson.startsWith("[")) {
                    vo.setImages(JSONUtil.toList(imagesJson, String.class));
                } else {
                    vo.setImages(Arrays.asList(imagesJson.split(",")));
                }
            }

            if (vo.getIsAnonymous()) {
                vo.setNickname("匿名用户");
                vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            } else {
                vo.setUsername(Convert.toStr(map.get("username")));
                vo.setNickname(Convert.toStr(map.get("nickname")));
                vo.setAvatar(Convert.toStr(map.get("avatar")));
            }

            if (currentUserId != null) {
                Integer likedCount = reviewLikeMapper.checkUserLiked(vo.getId(), currentUserId);
                vo.setIsLiked(likedCount > 0);
            } else {
                vo.setIsLiked(false);
            }

            return vo;
        }).collect(Collectors.toList());

        Integer total = reviewMapper.getReviewCount(
                dto.getProductId(),
                dto.getRating(),
                dto.getHasImages()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", total);
        result.put("page", dto.getPage());
        result.put("pageSize", dto.getPageSize());
        result.put("totalPages", (int) Math.ceil((double) total / dto.getPageSize()));

        log.info("查询成功: 共{}条评价", total);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, Long reviewId) {
        log.info("点赞操作: userId={}, reviewId={}", userId, reviewId);

        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            log.error("评价不存在: reviewId={}", reviewId);
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }

        Integer count = reviewLikeMapper.checkUserLiked(reviewId, userId);

        if (count > 0) {
            log.info("取消点赞: userId={}, reviewId={}", userId, reviewId);

            LambdaQueryWrapper<ReviewLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReviewLike::getReviewId, reviewId);
            wrapper.eq(ReviewLike::getUserId, userId);
            reviewLikeMapper.delete(wrapper);

            reviewMapper.decreaseLikeCount(reviewId);
        } else {
            log.info("添加点赞: userId={}, reviewId={}", userId, reviewId);

            ReviewLike like = new ReviewLike();
            like.setReviewId(reviewId);
            like.setUserId(userId);
            reviewLikeMapper.insert(like);

            reviewMapper.increaseLikeCount(reviewId);
        }

        log.info("点赞操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(ReplyReviewDTO dto) {
        log.info("商家回复评价: {}", dto);

        Review review = reviewMapper.selectById(dto.getReviewId());
        if (review == null) {
            log.error("评价不存在: reviewId={}", dto.getReviewId());
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }

        review.setReply(dto.getReplyContent());
        reviewMapper.updateById(review);

        log.info("回复成功: reviewId={}", dto.getReviewId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long userId, Long reviewId) {
        log.info("删除评价: userId={}, reviewId={}", userId, reviewId);

        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            log.error("评价不存在: reviewId={}", reviewId);
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }

        if (!review.getUserId().equals(userId)) {
            log.error("无权删除他人评价: userId={}, reviewUserId={}", userId, review.getUserId());
            throw new BusinessException(ResultCode.REVIEW_NOT_ALLOWED);
        }

        reviewMapper.deleteById(reviewId);

        log.info("删除成功: reviewId={}", reviewId);
    }

    @Override
    public Page<ReviewVO> getUserReviews(Long userId, Integer pageNum, Integer pageSize) {
        log.info("查询用户评价列表: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        int offset = (pageNum - 1) * pageSize;

        List<Map<String, Object>> reviewList = reviewMapper.getUserReviewList(userId, offset, pageSize);

        List<ReviewVO> voList = reviewList.stream().map(map -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(Convert.toLong(map.get("id")));
            vo.setProductId(Convert.toLong(map.get("product_id")));
            vo.setOrderId(Convert.toLong(map.get("order_id")));
            vo.setRating(Convert.toInt(map.get("rating")));
            vo.setContent(Convert.toStr(map.get("content")));
            vo.setLikeCount(Convert.toInt(map.get("like_count")));
            vo.setReply(Convert.toStr(map.get("reply")));

            Object createTime = map.get("create_time");
            if (createTime != null) {
                vo.setCreateTime(createTime.toString());
            }

            Object replyTime = map.get("reply_time");
            if (replyTime != null) {
                vo.setReplyTime(replyTime.toString());
            }

            String imagesStr = Convert.toStr(map.get("images"));
            if (imagesStr != null && !imagesStr.isEmpty()) {
                if (imagesStr.startsWith("[")) {
                    vo.setImageList(JSONUtil.toList(imagesStr, String.class));
                } else {
                    vo.setImageList(Arrays.asList(imagesStr.split(",")));
                }
            }

            vo.setProductName(Convert.toStr(map.get("productName")));
            vo.setProductImage(Convert.toStr(map.get("productImage")));

            return vo;
        }).collect(Collectors.toList());

        Integer total = reviewMapper.getUserReviewCount(userId);

        Page<ReviewVO> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(voList);

        log.info("查询成功: 共{}条评价", total);
        return result;
    }

    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        if (review.getIsAnonymous() == 0) {
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            } else {
                vo.setNickname("未知用户");
                vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            }
        } else {
            vo.setNickname("匿名用户");
            vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }

        if (review.getImages() != null && !review.getImages().isEmpty()) {
            if (review.getImages().startsWith("[")) {
                vo.setImageList(JSONUtil.toList(review.getImages(), String.class));
            } else {
                vo.setImageList(Arrays.asList(review.getImages().split(",")));
            }
        }

        return vo;
    }
}
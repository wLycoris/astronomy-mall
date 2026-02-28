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
    // 发布评价
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishReview(Long userId, PublishReviewDTO dto) {
        log.info("发布评价: userId={}, dto={}", userId, dto);

        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (!order.getUserId().equals(userId)) throw new BusinessException(ResultCode.REVIEW_NOT_ALLOWED);
        if (order.getStatus() != 3) throw new BusinessException(ResultCode.REVIEW_ORDER_NOT_FINISHED);

        // 检查是否已评价（包含已被删除的记录，不允许重复评价）
        Integer count = reviewMapper.checkProductReviewed(dto.getOrderId(), dto.getProductId(), userId);
        if (count > 0) throw new BusinessException(ResultCode.REVIEW_ALREADY_EXISTS);

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(dto.getProductId());
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setIsAnonymous(dto.getIsAnonymous() ? 1 : 0);
        review.setLikeCount(0);
        review.setReportCount(0);
        // 直接正常显示，举报后才进入审核
        review.setStatus(1);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            if (dto.getImages().size() > 9) throw new BusinessException(ResultCode.REVIEW_IMAGES_EXCEED_LIMIT);
            review.setImages(JSONUtil.toJsonStr(dto.getImages()));
        }

        reviewMapper.insert(review);
        log.info("评价发布成功: reviewId={}", review.getId());
    }

    // ============================================
    // 商品详情页：基础版评价列表
    // ============================================

    @Override
    public Page<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
                .eq(Review::getDeleted, 0)
                .orderByDesc(Review::getCreateTime);

        Page<Review> page = new Page<>(pageNum, pageSize);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        Page<ReviewVO> result = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        result.setRecords(reviewPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return result;
    }

    // ============================================
    // 评价统计
    // ============================================

    @Override
    public ReviewStatisticsVO getReviewStatistics(Long productId) {
        Map<String, Object> stats = reviewMapper.getReviewStatistics(productId);

        ReviewStatisticsVO vo = new ReviewStatisticsVO();
        vo.setProductId(productId);

        if (stats != null) {
            vo.setReviewCount(toInt(stats.get("reviewCount")));
            vo.setAvgRating(toDouble(stats.get("avgRating")));
            vo.setGoodRate(toDouble(stats.get("goodRate")));
            vo.setTotalLikes(toInt(stats.get("totalLikes")));
            vo.setFiveStar(toInt(stats.get("fiveStar")));
            vo.setFourStar(toInt(stats.get("fourStar")));
            vo.setThreeStar(toInt(stats.get("threeStar")));
            vo.setTwoStar(toInt(stats.get("twoStar")));
            vo.setOneStar(toInt(stats.get("oneStar")));
            vo.setHasImagesCount(toInt(stats.get("hasImagesCount")));
        }
        return vo;
    }

    // ============================================
    // 商品详情页：高级版评价列表（筛选+排序）
    // ============================================

    @Override
    public Map<String, Object> getReviewList(ReviewQueryDTO dto, Long currentUserId) {
        int offset = (dto.getPage() - 1) * dto.getPageSize();

        List<Map<String, Object>> reviewList = reviewMapper.getReviewList(
                dto.getProductId(), dto.getRating(), dto.getHasImages(),
                dto.getSortType(), offset, dto.getPageSize()
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
            vo.setIsTop(Convert.toInt(map.get("is_top")));
            vo.setProductName(Convert.toStr(map.get("productName")));
            vo.setProductImage(Convert.toStr(map.get("productImage")));

            Object createTime = map.get("create_time");
            if (createTime != null) vo.setCreateTime(createTime.toString());

            Object replyTime = map.get("reply_time");
            if (replyTime != null) vo.setReplyTime(replyTime.toString());

            String imagesJson = Convert.toStr(map.get("images"));
            if (imagesJson != null && !imagesJson.isEmpty()) {
                vo.setImages(imagesJson.startsWith("[")
                        ? JSONUtil.toList(imagesJson, String.class)
                        : Arrays.asList(imagesJson.split(",")));
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
                vo.setIsLiked(reviewLikeMapper.checkUserLiked(vo.getId(), currentUserId) > 0);
            } else {
                vo.setIsLiked(false);
            }

            return vo;
        }).collect(Collectors.toList());

        Integer total = reviewMapper.getReviewCount(dto.getProductId(), dto.getRating(), dto.getHasImages());

        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", total);
        result.put("page", dto.getPage());
        result.put("pageSize", dto.getPageSize());
        result.put("totalPages", (int) Math.ceil((double) total / dto.getPageSize()));
        return result;
    }

    // ============================================
    // 点赞
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);

        if (reviewLikeMapper.checkUserLiked(reviewId, userId) > 0) {
            LambdaQueryWrapper<ReviewLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReviewLike::getReviewId, reviewId).eq(ReviewLike::getUserId, userId);
            reviewLikeMapper.delete(wrapper);
            reviewMapper.decreaseLikeCount(reviewId);
        } else {
            ReviewLike like = new ReviewLike();
            like.setReviewId(reviewId);
            like.setUserId(userId);
            reviewLikeMapper.insert(like);
            reviewMapper.increaseLikeCount(reviewId);
        }
    }

    // ============================================
    // 商家回复（旧接口兼容）
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(ReplyReviewDTO dto) {
        Review review = reviewMapper.selectById(dto.getReviewId());
        if (review == null) throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);

        review.setReply(dto.getReplyContent());
        reviewMapper.updateById(review);
    }

    // ============================================
    // 用户自己删除评价
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        if (!review.getUserId().equals(userId)) throw new BusinessException(ResultCode.REVIEW_NOT_ALLOWED);

        reviewMapper.deleteById(reviewId);
    }

    // ============================================
    // 我的评价列表
    // 包含已被管理员删除的记录，deleted=1时前端显示"已被管理员删除"
    // ============================================

    @Override
    public Page<ReviewVO> getUserReviews(Long userId, Integer pageNum, Integer pageSize) {
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
            vo.setStatus(Convert.toInt(map.get("status")));

            Object createTime = map.get("create_time");
            if (createTime != null) vo.setCreateTime(createTime.toString());

            vo.setProductName(Convert.toStr(map.get("productName")));
            vo.setProductImage(Convert.toStr(map.get("productImage")));

            // status=0：已被管理员删除，清空敏感展示字段，只保留原始内容和时间
            if (Integer.valueOf(0).equals(vo.getStatus())) {
                vo.setReply(null);
                vo.setReplyTime(null);
                vo.setImages(null);
                vo.setImageList(null);
                return vo;
            }

            // 正常评价：填充完整字段
            vo.setReply(Convert.toStr(map.get("reply")));

            Object replyTime = map.get("reply_time");
            if (replyTime != null) vo.setReplyTime(replyTime.toString());

            String imagesStr = Convert.toStr(map.get("images"));
            if (imagesStr != null && !imagesStr.isEmpty()) {
                vo.setImageList(imagesStr.startsWith("[")
                        ? JSONUtil.toList(imagesStr, String.class)
                        : Arrays.asList(imagesStr.split(",")));
            }

            return vo;
        }).collect(Collectors.toList());

        Integer total = reviewMapper.getUserReviewCount(userId);

        Page<ReviewVO> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(voList);
        return result;
    }

    // ============================================
    // 举报评价
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportReview(Long userId, Long reviewId, String reason) {
        log.info("举报评价: userId={}, reviewId={}, reason={}", userId, reviewId, reason);

        // 1. 评价必须存在且正常显示
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || review.getDeleted() == 1) {
            throw new BusinessException("评价不存在");
        }
        if (review.getStatus() != 1) {
            throw new BusinessException("该评价已在审核中或已被删除");
        }

        // 2. 不能举报自己的评价
        if (review.getUserId().equals(userId)) {
            throw new BusinessException("不能举报自己的评价");
        }

        // 3. 每人对同一评价只能举报一次
        Integer reported = reviewMapper.checkUserReported(reviewId, userId);
        if (reported > 0) {
            throw new BusinessException("您已举报过该评价，请等待管理员处理");
        }

        // 4. 写入举报记录
        reviewMapper.insertReport(reviewId, userId, reason);

        // 5. 更新举报次数，达到阈值(3次)自动转为待审核
        reviewMapper.incrementReportCount(reviewId);

        log.info("举报成功: reviewId={}, 当前举报次数+1", reviewId);
    }

    // ============================================
    // 修改评价（直接UPDATE）
    // ============================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReview(Long userId, Long reviewId, PublishReviewDTO dto) {
        log.info("修改评价: userId={}, reviewId={}", userId, reviewId);

        Review review = reviewMapper.selectById(reviewId);
        if (review == null) throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        if (!review.getUserId().equals(userId)) throw new BusinessException(ResultCode.REVIEW_NOT_ALLOWED);
        if (Integer.valueOf(1).equals(review.getDeleted()))
            throw new BusinessException("该评价已被管理员删除，无法修改");

        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setIsAnonymous(dto.getIsAnonymous() ? 1 : 0);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            review.setImages(JSONUtil.toJsonStr(dto.getImages()));
        } else {
            review.setImages(null);
        }

        reviewMapper.updateById(review);
        log.info("修改评价成功: reviewId={}", reviewId);
    }

    // ============================================
    // 工具方法
    // ============================================

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
            vo.setImageList(review.getImages().startsWith("[")
                    ? JSONUtil.toList(review.getImages(), String.class)
                    : Arrays.asList(review.getImages().split(",")));
        }

        return vo;
    }

    private int toInt(Object obj) {
        return obj == null ? 0 : ((Number) obj).intValue();
    }

    private double toDouble(Object obj) {
        return obj == null ? 0.0 : ((Number) obj).doubleValue();
    }
}
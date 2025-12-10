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
 * 评价服务实现类
 *
 * 功能模块:
 * 1. 评价查询(基础版和高级版)
 * 2. 评价统计(星级分布、好评率等)
 * 3. 发布评价(含图片、匿名)
 * 4. 点赞管理(点赞/取消点赞)
 * 5. 商家回复
 * 6. 评价删除
 * 7. 我的评价列表
 *
 * 技术亮点:
 * 1. 类型安全转换(避免ClassCastException)
 * 2. 事务管理(@Transactional)
 * 3. 兼容JSON和逗号分隔两种图片格式
 * 4. 完善的异常处理
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
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
    // 原有方法保留(兼容性)
    // ============================================

    /**
     * 分页获取商品评价列表(基础版)
     *
     * 功能说明:
     * - 保留原有方法,确保兼容性
     * - 简单分页查询
     * - 不支持筛选和排序
     *
     * @param productId 商品ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价分页列表
     */
    @Override
    public Page<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize) {
        log.info("查询商品评价列表: productId={}, pageNum={}, pageSize={}", productId, pageNum, pageSize);

        // 1. 构建查询条件
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)      // 商品ID
                .eq(Review::getDeleted, 0)                // 未删除
                .orderByDesc(Review::getCreateTime);      // 按创建时间降序

        // 2. 分页查询
        Page<Review> page = new Page<>(pageNum, pageSize);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        // 3. 转换为VO对象
        Page<ReviewVO> result = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        List<ReviewVO> voList = reviewPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        result.setRecords(voList);

        log.info("查询成功,共{}条评价", result.getTotal());
        return result;
    }

    /**
     * 获取商品评价统计
     *
     * 功能说明:
     * - 一次SQL查询获取所有统计数据
     * - 性能优化:避免多次查询数据库
     * - 类型安全转换:避免ClassCastException
     *
     * 统计内容:
     * 1. 评价总数
     * 2. 平均评分
     * 3. 好评率(4-5星占比)
     * 4. 总点赞数
     * 5. 各星级评价数
     * 6. 有图评价数
     *
     * @param productId 商品ID
     * @return 评价统计VO
     */
    @Override
    public ReviewStatisticsVO getReviewStatistics(Long productId) {
        log.info("查询商品评价统计: productId={}", productId);

        // 1. 查询统计数据(一次SQL获取所有数据)
        Map<String, Object> stats = reviewMapper.getReviewStatistics(productId);

        // 2. 组装VO对象
        ReviewStatisticsVO vo = new ReviewStatisticsVO();
        vo.setProductId(productId);

        if (stats != null) {
            // ⚠️ 类型安全转换:使用Number接口避免ClassCastException
            // MySQL聚合函数返回的类型可能是BigDecimal或Long,统一用Number接收

            // 基础统计
            Object reviewCountObj = stats.get("reviewCount");
            vo.setReviewCount(reviewCountObj == null ? 0 : ((Number) reviewCountObj).intValue());

            Object avgObj = stats.get("avgRating");
            vo.setAvgRating(avgObj == null ? 0.0 : ((Number) avgObj).doubleValue());

            Object goodRateObj = stats.get("goodRate");
            vo.setGoodRate(goodRateObj == null ? 0.0 : ((Number) goodRateObj).doubleValue());

            Object totalLikesObj = stats.get("totalLikes");
            vo.setTotalLikes(totalLikesObj == null ? 0 : ((Number) totalLikesObj).intValue());

            // 星级分布
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

            // 有图评价数
            Object hasImagesCountObj = stats.get("hasImagesCount");
            vo.setHasImagesCount(hasImagesCountObj == null ? 0 : ((Number) hasImagesCountObj).intValue());
        }

        log.info("统计成功: 总评价={}, 平均分={}, 好评率={}%",
                vo.getReviewCount(), vo.getAvgRating(), vo.getGoodRate());
        return vo;
    }

    // ============================================
    // 新增方法
    // ============================================

    /**
     * 获取评价列表(高级版)
     *
     * 功能特性:
     * 1. 支持星级筛选
     * 2. 支持有图筛选
     * 3. 支持多种排序(最新/点赞/评分)
     * 4. 支持分页
     * 5. 关联用户和商品信息
     * 6. 判断当前用户是否已点赞
     *
     * @param dto 查询条件DTO
     * @param currentUserId 当前用户ID(用于判断是否已点赞,未登录可为null)
     * @return 评价列表(含分页信息)
     */
    @Override
    public Map<String, Object> getReviewList(ReviewQueryDTO dto, Long currentUserId) {
        log.info("查询评价列表(高级版): {}, currentUserId={}", dto, currentUserId);

        // 1. 计算分页偏移量
        int offset = (dto.getPage() - 1) * dto.getPageSize();

        // 2. 查询评价列表
        List<Map<String, Object>> reviewList = reviewMapper.getReviewList(
                dto.getProductId(),
                dto.getRating(),
                dto.getHasImages(),
                dto.getSortType(),
                offset,
                dto.getPageSize()
        );

        // 3. 转换为VO对象
        List<ReviewDetailVO> voList = reviewList.stream().map(map -> {
            ReviewDetailVO vo = new ReviewDetailVO();

            // 基本信息
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

            // 时间处理
            Object createTime = map.get("create_time");
            if (createTime != null) {
                vo.setCreateTime(createTime.toString());
            }

            Object replyTime = map.get("reply_time");
            if (replyTime != null) {
                vo.setReplyTime(replyTime.toString());
            }

            // ⚠️ 图片处理:兼容JSON和逗号分隔两种格式
            String imagesJson = Convert.toStr(map.get("images"));
            if (imagesJson != null && !imagesJson.isEmpty()) {
                if (imagesJson.startsWith("[")) {
                    // JSON格式: ["url1","url2"]
                    vo.setImages(JSONUtil.toList(imagesJson, String.class));
                } else {
                    // 逗号分隔格式: url1,url2
                    vo.setImages(Arrays.asList(imagesJson.split(",")));
                }
            }

            // 用户信息处理(匿名)
            if (vo.getIsAnonymous()) {
                vo.setNickname("匿名用户");
                vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            } else {
                vo.setUsername(Convert.toStr(map.get("username")));
                vo.setNickname(Convert.toStr(map.get("nickname")));
                vo.setAvatar(Convert.toStr(map.get("avatar")));
            }

            // 检查当前用户是否已点赞
            if (currentUserId != null) {
                Integer likedCount = reviewLikeMapper.checkUserLiked(vo.getId(), currentUserId);
                vo.setIsLiked(likedCount > 0);
            } else {
                vo.setIsLiked(false);
            }

            return vo;
        }).collect(Collectors.toList());

        // 4. 查询总数
        Integer total = reviewMapper.getReviewCount(
                dto.getProductId(),
                dto.getRating(),
                dto.getHasImages()
        );

        // 5. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", total);
        result.put("page", dto.getPage());
        result.put("pageSize", dto.getPageSize());
        result.put("totalPages", (int) Math.ceil((double) total / dto.getPageSize()));

        log.info("查询成功: 共{}条评价", total);
        return result;
    }

    /**
     * 发布评价
     *
     * 业务规则:
     * 1. 订单必须存在
     * 2. 订单必须属于当前用户
     * 3. 订单必须已完成(status=4)
     * 4. 订单不能重复评价
     * 5. 图片最多9张
     *
     * 事务管理:
     * - 使用@Transactional确保数据一致性
     * - 回滚条件:rollbackFor = Exception.class
     *
     * @param userId 用户ID
     * @param dto 评价信息DTO
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

        // ✅ 修复: 订单状态判断 - status=3 才是已完成
        // 3. 检查订单是否已完成(status=3)
        if (order.getStatus() != 3) {
            log.error("订单未完成,无法评价: orderId={}, status={}", dto.getOrderId(), order.getStatus());
            throw new BusinessException(ResultCode.REVIEW_ORDER_NOT_FINISHED);
        }

        // 4. 检查是否已评价(防止重复评价)
        Integer count = reviewMapper.checkOrderReviewed(dto.getOrderId(), userId);
        if (count > 0) {
            log.error("订单已评价: orderId={}, userId={}", dto.getOrderId(), userId);
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
        review.setStatus(1);  // 1=正常

        // 6. 处理图片列表(转JSON格式存储)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            // 检查图片数量
            if (dto.getImages().size() > 9) {
                log.error("图片数量超过限制: count={}", dto.getImages().size());
                throw new BusinessException(ResultCode.REVIEW_IMAGES_EXCEED_LIMIT);
            }
            // 转JSON格式存储
            review.setImages(JSONUtil.toJsonStr(dto.getImages()));
        }

        // 7. 插入数据库
        reviewMapper.insert(review);
        log.info("评价发布成功: reviewId={}", review.getId());
    }

    /**
     * 点赞/取消点赞
     *
     * 业务逻辑:
     * 1. 检查评价是否存在
     * 2. 检查是否已点赞
     * 3. 已点赞 -> 取消点赞(删除记录,点赞数-1)
     * 4. 未点赞 -> 添加点赞(插入记录,点赞数+1)
     *
     * 并发安全:
     * - 使用数据库唯一索引防止重复点赞
     * - 使用MySQL自增/自减操作保证点赞数准确
     *
     * 事务管理:
     * - 确保点赞记录和点赞数同步更新
     *
     * @param userId 用户ID
     * @param reviewId 评价ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, Long reviewId) {
        log.info("点赞操作: userId={}, reviewId={}", userId, reviewId);

        // 1. 检查评价是否存在
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            log.error("评价不存在: reviewId={}", reviewId);
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }

        // 2. 检查是否已点赞
        Integer count = reviewLikeMapper.checkUserLiked(reviewId, userId);

        if (count > 0) {
            // 已点赞 -> 取消点赞
            log.info("取消点赞: userId={}, reviewId={}", userId, reviewId);

            // 删除点赞记录
            LambdaQueryWrapper<ReviewLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReviewLike::getReviewId, reviewId);
            wrapper.eq(ReviewLike::getUserId, userId);
            reviewLikeMapper.delete(wrapper);

            // 点赞数-1(防止出现负数)
            reviewMapper.decreaseLikeCount(reviewId);
        } else {
            // 未点赞 -> 添加点赞
            log.info("添加点赞: userId={}, reviewId={}", userId, reviewId);

            // 插入点赞记录
            ReviewLike like = new ReviewLike();
            like.setReviewId(reviewId);
            like.setUserId(userId);
            reviewLikeMapper.insert(like);

            // 点赞数+1
            reviewMapper.increaseLikeCount(reviewId);
        }

        log.info("点赞操作成功");
    }

    /**
     * 商家回复评价
     *
     * 业务规则:
     * 1. 评价必须存在
     * 2. 可以多次修改回复内容
     *
     * @param dto 回复信息DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(ReplyReviewDTO dto) {
        log.info("商家回复评价: {}", dto);

        // 1. 检查评价是否存在
        Review review = reviewMapper.selectById(dto.getReviewId());
        if (review == null) {
            log.error("评价不存在: reviewId={}", dto.getReviewId());
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }

        // 2. 更新回复内容(reply_time由数据库自动更新)
        review.setReply(dto.getReplyContent());
        reviewMapper.updateById(review);

        log.info("回复成功: reviewId={}", dto.getReviewId());
    }

    /**
     * 删除评价(逻辑删除)
     *
     * 业务规则:
     * 1. 评价必须存在
     * 2. 只能删除自己的评价
     * 3. 使用逻辑删除(deleted=1)
     *
     * @param userId 用户ID
     * @param reviewId 评价ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long userId, Long reviewId) {
        log.info("删除评价: userId={}, reviewId={}", userId, reviewId);

        // 1. 检查评价是否存在
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            log.error("评价不存在: reviewId={}", reviewId);
            throw new BusinessException(ResultCode.REVIEW_NOT_FOUND);
        }

        // 2. 检查是否是本人的评价
        if (!review.getUserId().equals(userId)) {
            log.error("无权删除他人评价: userId={}, reviewUserId={}", userId, review.getUserId());
            throw new BusinessException(ResultCode.REVIEW_NOT_ALLOWED);
        }

        // 3. 逻辑删除(MyBatis-Plus自动处理)
        reviewMapper.deleteById(reviewId);

        log.info("删除成功: reviewId={}", reviewId);
    }

    /**
     * 获取用户的评价列表(我的评价)
     *
     * 功能说明:
     * - 查询指定用户的所有评价
     * - 关联查询商品信息
     * - 支持分页
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评价分页列表
     */
    @Override
    public Page<ReviewVO> getUserReviews(Long userId, Integer pageNum, Integer pageSize) {
        log.info("查询用户评价列表: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        // 1. 计算分页偏移量
        int offset = (pageNum - 1) * pageSize;

        // 2. 查询用户评价列表
        List<Map<String, Object>> reviewList = reviewMapper.getUserReviewList(userId, offset, pageSize);

        // 3. 转换为VO对象
        List<ReviewVO> voList = reviewList.stream().map(map -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(Convert.toLong(map.get("id")));
            vo.setProductId(Convert.toLong(map.get("product_id")));
            vo.setOrderId(Convert.toLong(map.get("order_id")));
            vo.setRating(Convert.toInt(map.get("rating")));
            vo.setContent(Convert.toStr(map.get("content")));
            vo.setLikeCount(Convert.toInt(map.get("like_count")));
            vo.setReply(Convert.toStr(map.get("reply")));

            // 时间处理
            Object createTime = map.get("create_time");
            if (createTime != null) {
                vo.setCreateTime(createTime.toString());
            }

            Object replyTime = map.get("reply_time");
            if (replyTime != null) {
                vo.setReplyTime(replyTime.toString());
            }

            // 图片处理(兼容两种格式)
            String imagesStr = Convert.toStr(map.get("images"));
            if (imagesStr != null && !imagesStr.isEmpty()) {
                if (imagesStr.startsWith("[")) {
                    vo.setImageList(JSONUtil.toList(imagesStr, String.class));
                } else {
                    vo.setImageList(Arrays.asList(imagesStr.split(",")));
                }
            }

            // 商品信息
            vo.setProductName(Convert.toStr(map.get("productName")));
            vo.setProductImage(Convert.toStr(map.get("productImage")));

            return vo;
        }).collect(Collectors.toList());

        // 4. 查询总数
        Integer total = reviewMapper.getUserReviewCount(userId);

        // 5. 组装分页结果
        Page<ReviewVO> result = new Page<>(pageNum, pageSize, total);
        result.setRecords(voList);

        log.info("查询成功: 共{}条评价", total);
        return result;
    }

    // ============================================
    // 私有方法
    // ============================================

    /**
     * Entity转VO(保留原有方法)
     *
     * 功能说明:
     * - 用于基础版评价列表
     * - 处理用户信息(匿名)
     * - 处理图片列表
     *
     * @param review 评价实体
     * @return 评价VO
     */
    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 用户信息处理(匿名)
        if (review.getIsAnonymous() == 0) {
            // 显示真实用户信息
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            } else {
                vo.setNickname("未知用户");
                vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            }
        } else {
            // 匿名用户
            vo.setNickname("匿名用户");
            vo.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }

        // 图片处理(兼容两种格式)
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            if (review.getImages().startsWith("[")) {
                // JSON格式
                vo.setImageList(JSONUtil.toList(review.getImages(), String.class));
            } else {
                // 逗号分隔格式
                vo.setImageList(Arrays.asList(review.getImages().split(",")));
            }
        }

        return vo;
    }
}
package com.astronomy.mall.module.product.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.product.dto.*;
import com.astronomy.mall.module.product.service.ReviewService;
import com.astronomy.mall.utils.JwtUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 评价控制器
 *
 * 功能模块:
 * 1. 评价查询(基础版和高级版)
 * 2. 评价统计
 * 3. 发布评价
 * 4. 点赞管理
 * 5. 商家回复
 * 6. 评价删除
 * 7. 我的评价
 *
 * 接口设计:
 * - 遵循RESTful规范
 * - 统一返回Result对象
 * - 完善的Swagger文档
 * - 参数自动验证(@Valid)
 *
 * 兼容性:
 * - 保留原有接口路径
 * - 新接口使用新路径
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@Api(tags = "商品评价接口")
@Validated
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    @Resource
    private JwtUtil jwtUtil;

    // ============================================
    // 工具方法
    // ============================================

    /**
     * 从请求头获取用户ID
     *
     * 功能说明:
     * 1. 从Authorization请求头获取Token
     * 2. 去除"Bearer "前缀
     * 3. 解析Token获取用户ID
     *
     * 使用场景:
     * - 需要登录的接口
     * - 判断当前用户是否已点赞
     *
     * @param request HTTP请求对象
     * @return 用户ID(未登录返回null)
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 1. 获取Authorization请求头
        String token = request.getHeader("Authorization");

        // 2. 检查Token是否存在
        if (token != null && token.startsWith("Bearer ")) {
            // 3. 去除"Bearer "前缀(7个字符)
            token = token.substring(7);

            // 4. 解析Token获取用户ID
            return jwtUtil.getUserIdFromToken(token);
        }

        // 5. 未登录返回null
        return null;
    }

    // ============================================
    // 原有接口(保留兼容性)
    // ============================================

    /**
     * 分页获取商品评价列表(基础版)
     *
     * 功能说明:
     * - 简单的分页查询
     * - 不支持筛选和排序
     * - 保留原有接口路径
     *
     * 接口信息:
     * - 请求方式: GET
     * - 接口路径: /api/review/list/{productId}
     * - 是否需要登录: 否
     *
     * @param productId 商品ID(路径参数)
     * @param pageNum 页码(默认1)
     * @param pageSize 每页数量(默认10)
     * @return 评价分页列表
     */
    @GetMapping("/list/{productId}")
    @ApiOperation("分页获取商品评价列表(基础版)")
    public Result<Page<ReviewVO>> getProductReviews(
            @ApiParam("商品ID") @PathVariable Long productId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("GET /api/review/list/{}: pageNum={}, pageSize={}", productId, pageNum, pageSize);

        Page<ReviewVO> page = reviewService.getProductReviews(productId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取商品评价统计
     *
     * 功能说明:
     * - 获取评价总数、平均分、好评率
     * - 获取星级分布
     * - 获取有图评价数
     *
     * 接口信息:
     * - 请求方式: GET
     * - 接口路径: /api/review/statistics/{productId}
     * - 是否需要登录: 否
     *
     * @param productId 商品ID(路径参数)
     * @return 评价统计信息
     */
    @GetMapping("/statistics/{productId}")
    @ApiOperation("获取商品评价统计")
    public Result<ReviewStatisticsVO> getReviewStatistics(
            @ApiParam("商品ID") @PathVariable Long productId) {

        log.info("GET /api/review/statistics/{}", productId);

        ReviewStatisticsVO statistics = reviewService.getReviewStatistics(productId);
        return Result.success(statistics);
    }

    // ============================================
    // 新增接口
    // ============================================

    /**
     * 获取评价列表(高级版 - 支持筛选和排序)
     *
     * 功能特性:
     * 1. 支持星级筛选(rating参数)
     * 2. 支持有图筛选(hasImages参数)
     * 3. 支持多种排序(sortType参数)
     * 4. 支持分页(page, pageSize参数)
     * 5. 返回点赞状态(需要登录)
     *
     * 接口信息:
     * - 请求方式: GET
     * - 接口路径: /api/review/list/advanced
     * - 是否需要登录: 否(但登录后可显示点赞状态)
     *
     * @param request HTTP请求对象
     * @param dto 查询条件DTO(自动验证)
     * @return 评价列表(含分页信息)
     */
    @GetMapping("/list/advanced")
    @ApiOperation("获取评价列表(高级版 - 支持筛选和排序)")
    public Result<Map<String, Object>> getReviewListAdvanced(
            HttpServletRequest request,
            @Valid ReviewQueryDTO dto) {

        log.info("GET /api/review/list/advanced: {}", dto);

        // 获取当前用户ID(用于判断是否已点赞,未登录为null)
        Long currentUserId = getUserIdFromRequest(request);

        Map<String, Object> result = reviewService.getReviewList(dto, currentUserId);
        return Result.success(result);
    }

    /**
     * 发布评价
     *
     * 业务规则:
     * 1. 必须登录
     * 2. 订单必须已完成
     * 3. 订单不能重复评价
     * 4. 图片最多9张
     *
     * 接口信息:
     * - 请求方式: POST
     * - 接口路径: /api/review/publish
     * - 是否需要登录: 是
     * - Content-Type: application/json
     *
     * @param request HTTP请求对象
     * @param dto 评价信息DTO(自动验证)
     * @return 操作结果
     */
    @PostMapping("/publish")
    @ApiOperation("发布评价")
    public Result<Void> publishReview(
            HttpServletRequest request,
            @Valid @RequestBody PublishReviewDTO dto) {

        log.info("POST /api/review/publish: {}", dto);

        // 1. 获取当前用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            log.warn("未登录用户尝试发布评价");
            return Result.error(401, "未登录");
        }

        // 2. 发布评价
        reviewService.publishReview(userId, dto);

        return Result.success();
    }

    /**
     * 点赞/取消点赞
     *
     * 业务逻辑:
     * - 已点赞 -> 取消点赞
     * - 未点赞 -> 添加点赞
     *
     * 接口信息:
     * - 请求方式: POST
     * - 接口路径: /api/review/like/{reviewId}
     * - 是否需要登录: 是
     *
     * @param request HTTP请求对象
     * @param reviewId 评价ID(路径参数)
     * @return 操作结果
     */
    @PostMapping("/like/{reviewId}")
    @ApiOperation("点赞/取消点赞")
    public Result<Void> toggleLike(
            HttpServletRequest request,
            @ApiParam("评价ID") @PathVariable Long reviewId) {

        log.info("POST /api/review/like/{}", reviewId);

        // 1. 获取当前用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            log.warn("未登录用户尝试点赞");
            return Result.error(401, "未登录");
        }

        // 2. 点赞/取消点赞
        reviewService.toggleLike(userId, reviewId);

        return Result.success();
    }

    /**
     * 商家回复评价
     *
     * 业务规则:
     * - 评价必须存在
     * - 可以多次修改回复
     *
     * 接口信息:
     * - 请求方式: POST
     * - 接口路径: /api/review/reply
     * - 是否需要登录: 是(需要管理员权限)
     * - Content-Type: application/json
     *
     * @param dto 回复信息DTO(自动验证)
     * @return 操作结果
     */
    @PostMapping("/reply")
    @ApiOperation("商家回复评价")
    public Result<Void> replyReview(@Valid @RequestBody ReplyReviewDTO dto) {

        log.info("POST /api/review/reply: {}", dto);

        reviewService.replyReview(dto);

        return Result.success();
    }

    /**
     * 删除评价
     *
     * 业务规则:
     * 1. 必须登录
     * 2. 只能删除自己的评价
     * 3. 使用逻辑删除
     *
     * 接口信息:
     * - 请求方式: DELETE
     * - 接口路径: /api/review/{reviewId}
     * - 是否需要登录: 是
     *
     * @param request HTTP请求对象
     * @param reviewId 评价ID(路径参数)
     * @return 操作结果
     */
    @DeleteMapping("/{reviewId}")
    @ApiOperation("删除评价")
    public Result<Void> deleteReview(
            HttpServletRequest request,
            @ApiParam("评价ID") @PathVariable Long reviewId) {

        log.info("DELETE /api/review/{}", reviewId);

        // 1. 获取当前用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            log.warn("未登录用户尝试删除评价");
            return Result.error(401, "未登录");
        }

        // 2. 删除评价
        reviewService.deleteReview(userId, reviewId);

        return Result.success();
    }

    @PutMapping("/{reviewId}")
    @ApiOperation("修改评价")
    public Result<Void> updateReview(
            HttpServletRequest request,
            @PathVariable Long reviewId,
            @Valid @RequestBody PublishReviewDTO dto) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.error(401, "未登录");
        reviewService.updateReview(userId, reviewId, dto);
        return Result.success();
    }

    /**
     * 获取我的评价列表
     *
     * 功能说明:
     * - 查询当前用户的所有评价
     * - 关联显示商品信息
     * - 支持分页
     *
     * 接口信息:
     * - 请求方式: GET
     * - 接口路径: /api/review/my/list
     * - 是否需要登录: 是
     *
     * @param request HTTP请求对象
     * @param pageNum 页码(默认1)
     * @param pageSize 每页数量(默认10)
     * @return 我的评价列表
     */
    @GetMapping("/my/list")
    @ApiOperation("获取我的评价列表")
    public Result<Page<ReviewVO>> getMyReviews(
            HttpServletRequest request,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("GET /api/review/my/list: pageNum={}, pageSize={}", pageNum, pageSize);

        // 1. 获取当前用户ID
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            log.warn("未登录用户尝试查看我的评价");
            return Result.error(401, "未登录");
        }

        // 2. 查询用户评价列表
        Page<ReviewVO> page = reviewService.getUserReviews(userId, pageNum, pageSize);

        return Result.success(page);
    }

    // ============================================
    // 兼容性接口
    // ============================================

    /**
     * 获取评价统计(兼容接口)
     *
     * 功能说明:
     * - 支持Query参数方式传递商品ID
     * - 与路径参数方式效果相同
     *
     * 接口信息:
     * - 请求方式: GET
     * - 接口路径: /api/review/statistics?productId=1
     * - 是否需要登录: 否
     *
     * @param productId 商品ID(Query参数)
     * @return 评价统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取商品评价统计(兼容接口)")
    public Result<ReviewStatisticsVO> getReviewStatisticsByParam(
            @ApiParam("商品ID") @RequestParam Long productId) {

        log.info("GET /api/review/statistics?productId={}", productId);

        ReviewStatisticsVO statistics = reviewService.getReviewStatistics(productId);
        return Result.success(statistics);
    }

    // ============================================
    // 举报评价
    // ============================================

    /**
     * 举报评价
     * - 请求方式: POST
     * - 路径: /api/review/report/{reviewId}
     * - 需要登录
     * - Body: { "reason": "举报原因" }
     * - 举报次数达到3次，评价自动转为待审核(status=2)，从商品页隐藏
     */
    @PostMapping("/report/{reviewId}")
    @ApiOperation("举报评价")
    public Result<Void> reportReview(
            HttpServletRequest request,
            @ApiParam("评价ID") @PathVariable Long reviewId,
            @RequestParam(required = false, defaultValue = "") String reason) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.error(401, "未登录");
        reviewService.reportReview(userId, reviewId, reason);
        return Result.success();
    }
}
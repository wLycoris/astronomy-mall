package com.astronomy.mall.module.course.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.course.dto.CourseQueryDTO;
import com.astronomy.mall.module.course.service.CourseService;
import com.astronomy.mall.module.course.vo.CourseChapterVO;
import com.astronomy.mall.module.course.vo.CourseVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天文课程 用户端 Controller
 *
 * 接口列表:
 *   GET  /api/course/list                    课程列表（分页+多条件筛选）
 *   GET  /api/course/{id}                    课程详情（含章节目录）
 *   GET  /api/course/chapter/{chapterId}     章节内容（副作用：记录进度）
 *   POST /api/course/favorite/toggle/{id}    收藏/取消收藏（需登录）
 *   GET  /api/course/favorite/list           我的收藏列表（需登录）
 *   GET  /api/course/history                 学习历史（需登录）
 *   GET  /api/course/{id}/reviews            评价列表（占位，本期返回空）
 *   GET  /api/course/recommend               推荐课程（5.4新增，可选认证）
 *
 * 📌 认证说明:
 * - 课程列表/详情/章节: 可选认证（登录时附加 isFavorite/lastChapterId）
 * - 收藏/历史: 需要登录（Controller 层校验 userId != null）
 * - 推荐课程: 可选认证（未登录返回热门兜底，登录返回个性化推荐）
 *
 * 📌 WebMvcConfig 说明:
 * /api/course/** 已在 OPTIONAL_AUTH_LIST（可选认证列表）中，
 * Token 存在则 JwtInterceptor 解析并 setAttribute("userId", ...)，
 * Token 不存在则跳过，userId 为 null。
 */
@Slf4j
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
@Api(tags = "天文课程-用户端")
public class CourseController {

    private final CourseService courseService;

    // ==================== 工具方法：从request获取用户ID ====================

    /**
     * 从 request 中获取当前登录用户ID
     * JwtInterceptor 成功解析后会把 userId 存入 request.setAttribute("userId", ...)
     * 未登录时返回 null（课程接口支持游客访问）
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) return null;
        return Long.parseLong(userIdObj.toString());
    }

    // ==================== 核心接口 ====================

    /**
     * 课程列表（分页+多条件筛选）
     * 无需登录，登录时返回 isFavorite/lastChapterId 字段
     *
     * 参数:
     *   pageNum    页码（默认1）
     *   pageSize   每页条数（默认12）
     *   type       课程类型（null-全部 0-视频课 1-书本课）
     *   difficulty 难度（null-全部 1-入门 2-进阶 3-高级）
     *   keyword    关键词（搜索标题）
     *   tags       多标签AND筛选（逗号分隔，如"深空摄影,望远镜使用"）
     */
    @GetMapping("/list")
    @ApiOperation("课程列表")
    public Result<IPage<CourseVO>> getCourseList(
            CourseQueryDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(courseService.getCourseList(dto, userId));
    }

    /**
     * 课程详情（含章节目录，不含章节正文）
     * 无需登录，登录时返回 isFavorite/lastChapterId 字段
     * 同时 view_count +1
     */
    @GetMapping("/{id}")
    @ApiOperation("课程详情")
    public Result<CourseVO> getCourseDetail(
            @ApiParam("课程ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(courseService.getCourseDetail(id, userId));
    }

    /**
     * 获取章节完整内容（含 videoUrl 或 content 正文）
     * 无需登录（但登录后才会记录学习进度）
     *
     * 副作用（登录用户）:
     * - UPSERT tb_course_progress（记录 last_chapter_id + 追加 completed_chapters）
     * - 检测完课（排除APOD/火星课），完课时异步发通知
     */
    @GetMapping("/chapter/{chapterId}")
    @ApiOperation("章节内容（自动记录进度）")
    public Result<CourseChapterVO> getCourseChapter(
            @ApiParam("章节ID") @PathVariable Long chapterId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(courseService.getChapter(chapterId, userId));
    }

    // ==================== 5.4 推荐课程接口（可选认证）====================

    /**
     * 推荐课程（根据近3个月购买商品标签匹配，最多6个）
     *
     * 认证: 可选认证
     *   - 未登录: 返回热门兜底（按 view_count 倒序前6），前端 getToken() 为空不显示区块
     *   - 已登录且无购买记录: 返回热门兜底
     *   - 已登录且有购买记录: 返回个性化推荐（标签匹配，排除已学习课程）
     *
     * 前端显示控制:
     *   CourseList.vue 用 v-if="getToken() && recommendList.length > 0" 控制
     *   未登录时前端根本不调用此接口，节省服务器资源
     *
     * @param request HttpServletRequest（由 JwtInterceptor 可选注入 userId）
     * @return 推荐课程列表，最多6个 CourseVO
     */
    @GetMapping("/recommend")
    @ApiOperation("推荐课程（基于购买商品标签，最多6个）")
    public Result<List<CourseVO>> getRecommendCourses(HttpServletRequest request) {
        // 可选认证：从 request 取 userId，未登录为 null
        Long userId = getCurrentUserId(request);
        List<CourseVO> list = courseService.getRecommendCourses(userId);
        return Result.success(list);
    }

    // ==================== 收藏接口（需登录）====================

    /**
     * 收藏/取消收藏 课程（幂等）
     * 已收藏 → 取消；未收藏 → 收藏
     * 需要登录
     */
    @PostMapping("/favorite/toggle/{courseId}")
    @ApiOperation("收藏/取消收藏课程")
    public Result<Map<String, Object>> toggleFavorite(
            @ApiParam("课程ID") @PathVariable Long courseId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean isFavorite = courseService.toggleFavorite(courseId, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", isFavorite);
        data.put("message", isFavorite ? "收藏成功" : "已取消收藏");
        return Result.success(data);
    }

    /**
     * 我的课程收藏列表（分页）
     * 需要登录
     */
    @GetMapping("/favorite/list")
    @ApiOperation("我的课程收藏列表")
    public Result<IPage<CourseVO>> getMyFavoriteList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(courseService.getMyFavoriteList(userId, pageNum, pageSize));
    }

    /**
     * 我的学习历史（按最近学习时间倒序）
     * 需要登录
     */
    @GetMapping("/history")
    @ApiOperation("我的学习历史")
    public Result<IPage<CourseVO>> getMyHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(courseService.getMyHistory(userId, pageNum, pageSize));
    }

    /**
     * 课程评价列表（本期占位，返回空列表）
     * 详情页底部「课程评价功能即将开放」提示用
     */
    @GetMapping("/{id}/reviews")
    @ApiOperation("课程评价列表（占位）")
    public Result<?> getCourseReviews(@PathVariable Long id) {
        // 本期不开放评价功能，返回空列表占位
        return Result.success(new ArrayList<>());
    }
}
package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.PostAuditDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 论坛后台管理 Controller（管理员端）
 *
 * 接口前缀: /api/admin/post
 * 权限: AdminInterceptor 拦截 /api/admin/** → 校验 role=1
 *
 * 📌 接口规划（8个，7.7实现）:
 *   GET    /list               - 帖子列表（分页+状态+关键词筛选）
 *   POST   /audit/{id}         - 审核（approve/reject）
 *   POST   /top/{id}           - 置顶/取消置顶
 *   DELETE /{id}               - 删除帖子（status=4）
 *   GET    /comment/list       - 评论列表（分页+按帖子筛选）
 *   DELETE /comment/{id}       - 删除评论
 *   GET    /stats              - 论坛数据统计
 *   GET    /pending/count      - 待审核数量（导航角标用）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/post")
@Api(tags = "论坛后台 - 帖子管理")
public class AdminPostController {

    // TODO 7.7 注入 PostService / CommentService

    @GetMapping("/list")
    @ApiOperation("帖子列表（管理员）")
    public Result<Map<String, Object>> listPosts(
            @ApiParam("状态筛选") @RequestParam(required = false) Integer status,
            @ApiParam("关键词搜索") @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // TODO 7.7 实现
        return Result.error("后台帖子列表功能待实现");
    }

    @PostMapping("/audit/{id}")
    @ApiOperation("审核帖子（通过/拒绝）")
    public Result<Void> auditPost(@PathVariable Long id,
                                  @RequestBody PostAuditDTO dto,
                                  HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        // TODO 7.7 实现
        log.info("管理员 {} 审核帖子 {}: {}", adminId, id, dto.getAction());
        return Result.error("审核功能待实现");
    }

    @PostMapping("/top/{id}")
    @ApiOperation("置顶/取消置顶帖子")
    public Result<Void> toggleTop(@PathVariable Long id,
                                  HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        // TODO 7.7 实现
        log.info("管理员 {} 操作帖子 {} 置顶", adminId, id);
        return Result.error("置顶功能待实现");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除帖子（管理员，status=4）")
    public Result<Void> deletePost(@PathVariable Long id,
                                   HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        // TODO 7.7 实现
        log.info("管理员 {} 删除帖子 {}", adminId, id);
        return Result.error("管理员删除功能待实现");
    }

    @GetMapping("/comment/list")
    @ApiOperation("评论列表（管理员）")
    public Result<Map<String, Object>> listComments(
            @ApiParam("按帖子ID筛选") @RequestParam(required = false) Long postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // TODO 7.7 实现
        return Result.error("评论列表功能待实现");
    }

    @DeleteMapping("/comment/{id}")
    @ApiOperation("删除评论（管理员）")
    public Result<Void> deleteComment(@PathVariable Long id,
                                      HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        // TODO 7.7 实现
        log.info("管理员 {} 删除评论 {}", adminId, id);
        return Result.error("管理员删除评论功能待实现");
    }

    @GetMapping("/stats")
    @ApiOperation("论坛数据统计")
    public Result<Map<String, Object>> getStats() {
        // TODO 7.7 实现
        return Result.error("统计功能待实现");
    }

    @GetMapping("/pending/count")
    @ApiOperation("待审核帖子数量（角标用）")
    public Result<Integer> getPendingCount() {
        // TODO 7.7 实现
        return Result.error("待审核计数功能待实现");
    }
}

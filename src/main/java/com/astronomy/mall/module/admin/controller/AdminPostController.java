package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.PostAuditDTO;
import com.astronomy.mall.module.admin.dto.PostCommentQueryDTO;
import com.astronomy.mall.module.admin.dto.PostQueryDTO;
import com.astronomy.mall.module.admin.service.AdminPostService;
import com.astronomy.mall.module.admin.vo.ForumStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 论坛后台管理 Controller（管理员端，7.7 实现）
 *
 * 接口前缀: /api/admin/post
 * 权限: AdminInterceptor 拦截 /api/admin/** → 校验 role=1
 *
 * 📌 8 个端点（7.7 完成）:
 *   GET    /list               - 帖子列表（分页+状态+关键词筛选）
 *   POST   /audit/{id}         - 审核（approve/reject）
 *   POST   /top/{id}           - 加入/取消推荐（幂等，is_top 字段复用为"推荐信号"）
 *   DELETE /{id}               - 删除帖子（status=4 标记）
 *   GET    /comment/list       - 评论列表（分页+按帖子筛选）
 *   DELETE /comment/{id}       - 删除评论（@TableLogic 软删）
 *   GET    /stats              - 论坛数据统计
 *   GET    /pending/count      - 待审核数量（导航角标用）
 *
 * 📌 写操作均加 @AdminLog 走 AOP 审计日志
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/post")
@Api(tags = "论坛后台 - 帖子管理")
public class AdminPostController {

    @Autowired
    private AdminPostService adminPostService;

    // ============================================================
    // 1. 帖子列表
    // ============================================================
    @GetMapping("/list")
    @ApiOperation("帖子列表（管理员视角，可按状态/关键词筛选）")
    public Result<Map<String, Object>> listPosts(PostQueryDTO dto) {
        Map<String, Object> data = adminPostService.listPosts(dto);
        return Result.success(data);
    }

    // ============================================================
    // 2. 审核帖子
    // ============================================================
    @PostMapping("/audit/{id}")
    @ApiOperation("审核帖子（action=approve/reject，reject需填reason）")
    @AdminLog("审核论坛帖子")
    public Result<Void> auditPost(@PathVariable Long id,
                                  @Validated @RequestBody PostAuditDTO dto,
                                  HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminPostService.auditPost(id, dto, adminId);
        return Result.success();
    }

    // ============================================================
    // 3. 加入 / 取消"管理员推荐"
    //    URL 仍保持 /top/{id} 以避免前端 / 文档大幅改动；
    //    业务语义已从"置顶"重新定位为"推荐信号"，供 8.x 推荐算法使用。
    // ============================================================
    @PostMapping("/top/{id}")
    @ApiOperation("加入/取消推荐（幂等切换，is_top 字段复用为推荐信号）")
    @AdminLog("切换论坛帖子推荐")
    public Result<Void> toggleTop(@PathVariable Long id,
                                  HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminPostService.toggleRecommendPost(id, adminId);
        return Result.success();
    }

    // ============================================================
    // 4. 删除帖子（status=4，保留记录）
    // ============================================================
    @DeleteMapping("/{id}")
    @ApiOperation("删除帖子（管理员删除，status=4，作者仍可看到删除提示）")
    @AdminLog("删除论坛帖子")
    public Result<Void> deletePost(@PathVariable Long id,
                                   HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminPostService.deletePost(id, adminId);
        return Result.success();
    }

    // ============================================================
    // 5. 评论列表
    // ============================================================
    @GetMapping("/comment/list")
    @ApiOperation("评论列表（管理员视角，可按帖子/关键词筛选）")
    public Result<Map<String, Object>> listComments(PostCommentQueryDTO dto) {
        Map<String, Object> data = adminPostService.listComments(dto);
        return Result.success(data);
    }

    // ============================================================
    // 6. 删除评论
    // ============================================================
    @DeleteMapping("/comment/{id}")
    @ApiOperation("删除评论（管理员，@TableLogic 软删）")
    @AdminLog("删除论坛评论")
    public Result<Void> deleteComment(@PathVariable Long id,
                                      HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminPostService.deleteComment(id, adminId);
        return Result.success();
    }

    // ============================================================
    // 7. 论坛数据统计
    // ============================================================
    @GetMapping("/stats")
    @ApiOperation("论坛数据统计（今日/总数/状态分布/7天趋势）")
    public Result<ForumStatsVO> getStats() {
        return Result.success(adminPostService.getStats());
    }

    // ============================================================
    // 8. 待审核帖子数量（导航角标）
    // ============================================================
    @GetMapping("/pending/count")
    @ApiOperation("待审核帖子数量（用于侧边栏角标）")
    public Result<Long> getPendingCount() {
        return Result.success(adminPostService.getPendingCount());
    }
}

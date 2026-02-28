package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.ReviewAuditDTO;
import com.astronomy.mall.module.admin.dto.ReviewQueryDTO;
import com.astronomy.mall.module.admin.dto.ReviewReplyDTO;
import com.astronomy.mall.module.admin.service.AdminReviewService;
import com.astronomy.mall.module.admin.vo.AdminReviewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 后台评价管理 Controller
 *
 * 📌 接口清单（6个）:
 * GET    /api/admin/review/list           - 评价列表（分页）
 * GET    /api/admin/review/detail/:id     - 评价详情
 * POST   /api/admin/review/reply/:id      - 回复评价
 * POST   /api/admin/review/audit/:id      - 审核评价
 * POST   /api/admin/review/top/:id        - 置顶/取消置顶
 * DELETE /api/admin/review/delete/:id     - 删除评价
 *
 * 📌 权限: 管理员（AdminInterceptor 已拦截 /api/admin/**）
 */
@Slf4j
@Api(tags = "后台-评价管理")
@RestController
@RequestMapping("/api/admin/review")
public class AdminReviewController {

    @Autowired
    private AdminReviewService adminReviewService;

    // ============================================================
    // 1. 评价列表（分页）
    // ============================================================

    @ApiOperation("评价列表（分页）")
    @GetMapping("/list")
    public Result<Page<AdminReviewVO>> getReviewList(ReviewQueryDTO dto) {
        Page<AdminReviewVO> page = adminReviewService.getReviewList(dto);
        return Result.success(page);
    }

    // ============================================================
    // 2. 评价详情
    // ============================================================

    @ApiOperation("评价详情")
    @GetMapping("/detail/{id}")
    public Result<AdminReviewVO> getReviewDetail(
            @ApiParam("评价ID") @PathVariable Long id) {
        AdminReviewVO vo = adminReviewService.getReviewDetail(id);
        return Result.success(vo);
    }

    // ============================================================
    // 3. 商家回复评价
    // ============================================================

    @ApiOperation("回复评价")
    @AdminLog("回复评价")
    @PostMapping("/reply/{id}")
    public Result<Void> replyReview(
            @ApiParam("评价ID") @PathVariable Long id,
            @Validated @RequestBody ReviewReplyDTO dto) {
        adminReviewService.replyReview(id, dto);
        return Result.success();
    }

    // ============================================================
    // 4. 审核评价
    // ============================================================

    @ApiOperation("审核评价（action=1通过，action=0拒绝）")
    @AdminLog("审核评价")
    @PostMapping("/audit/{id}")
    public Result<Void> auditReview(
            @ApiParam("评价ID") @PathVariable Long id,
            @Validated @RequestBody ReviewAuditDTO dto) {
        adminReviewService.auditReview(id, dto);
        return Result.success();
    }

    // ============================================================
    // 5. 置顶 / 取消置顶
    // ============================================================

    @ApiOperation("置顶/取消置顶评价（自动切换）")
    @AdminLog("切换评价置顶")
    @PostMapping("/top/{id}")
    public Result<Void> toggleTop(
            @ApiParam("评价ID") @PathVariable Long id) {
        adminReviewService.toggleTopReview(id);
        return Result.success();
    }

    // ============================================================
    // 6. 删除评价
    // ============================================================

    @ApiOperation("删除评价（逻辑删除）")
    @AdminLog("删除评价")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteReview(
            @ApiParam("评价ID") @PathVariable Long id) {
        adminReviewService.deleteReview(id);
        return Result.success();
    }
}
package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.service.AdminRecyclingService;
import com.astronomy.mall.module.admin.vo.AdminRecyclingVO;
import com.astronomy.mall.utils.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 二手回收管理控制器
 *
 * 📌 基础路径: /api/admin/recycling
 * 📌 需要管理员权限（AdminInterceptor 鉴权）
 *
 * 接口列表（5个）:
 *   GET    /api/admin/recycling/list         - 申请列表(分页)
 *   GET    /api/admin/recycling/detail/:id   - 申请详情
 *   POST   /api/admin/recycling/quote/:id    - 提交报价
 *   POST   /api/admin/recycling/reject/:id   - 拒绝申请
 *   POST   /api/admin/recycling/arrange/:id  - 安排取件
 *   POST   /api/admin/recycling/complete/:id - 标记已回收(触发余额发放)
 */
@RestController
@RequestMapping("/api/admin/recycling")
@RequiredArgsConstructor
public class AdminRecyclingController {

    private final AdminRecyclingService adminRecyclingService;

    /**
     * 1. 回收申请列表（分页）
     * GET /api/admin/recycling/list
     */
    @GetMapping("/list")
    public Result<Page<AdminRecyclingVO>> list(RecyclingQueryDTO dto) {
        Page<AdminRecyclingVO> page = adminRecyclingService.getList(dto);
        return Result.success(page);
    }

    /**
     * 2. 申请详情
     * GET /api/admin/recycling/detail/:id
     */
    @GetMapping("/detail/{id}")
    public Result<AdminRecyclingVO> detail(@PathVariable Long id) {
        AdminRecyclingVO vo = adminRecyclingService.getDetail(id);
        return Result.success(vo);
    }

    /**
     * 3. 提交报价（待审核 → 已报价）
     * POST /api/admin/recycling/quote/:id
     */
    @PostMapping("/quote/{id}")
    @AdminLog("提交回收报价")
    public Result<Void> quote(@PathVariable Long id,
                              @Validated @RequestBody RecyclingQuoteDTO dto) {
        Long adminId = UserContext.getUserId();
        adminRecyclingService.submitQuote(id, dto, adminId);
        return Result.success();
    }

    /**
     * 4. 拒绝申请（待审核 → 已拒绝）
     * POST /api/admin/recycling/reject/:id
     */
    @PostMapping("/reject/{id}")
    @AdminLog("拒绝回收申请")
    public Result<Void> reject(@PathVariable Long id,
                               @Validated @RequestBody RecyclingRejectDTO dto) {
        Long adminId = UserContext.getUserId();
        adminRecyclingService.rejectApply(id, dto, adminId);
        return Result.success();
    }

    /**
     * 5. 安排取件（用户确认 → 待取件）
     * POST /api/admin/recycling/arrange/:id
     */
    @PostMapping("/arrange/{id}")
    @AdminLog("安排回收取件")
    public Result<Void> arrange(@PathVariable Long id,
                                @Validated @RequestBody RecyclingArrangeDTO dto) {
        Long adminId = UserContext.getUserId();
        adminRecyclingService.arrangePickup(id, dto, adminId);
        return Result.success();
    }

    /**
     * 6. 标记已回收 → 自动发放余额到用户钱包
     * POST /api/admin/recycling/complete/:id
     *
     * ⚠️ 重要: 此接口会触发 BalanceService.changeBalance()，请确保钱包模块已正常运行
     */
    @PostMapping("/complete/{id}")
    @AdminLog("完成回收发放余额")
    public Result<Void> complete(@PathVariable Long id) {
        Long adminId = UserContext.getUserId();
        adminRecyclingService.completeRecycling(id, adminId);
        return Result.success();
    }
}
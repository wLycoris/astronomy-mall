package com.astronomy.mall.module.aftersale.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.aftersale.dto.RecyclingApplyDTO;
import com.astronomy.mall.module.aftersale.service.RecyclingService;
import com.astronomy.mall.module.aftersale.vo.RecyclingVO;
import com.astronomy.mall.utils.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 二手回收控制器（用户端）
 *
 * 📌 基础路径: /api/recycling
 * 📌 需要用户登录（JwtInterceptor 鉴权）
 *
 * 接口列表（6个）:
 *   POST   /api/recycling/submit             - 提交回收申请
 *   GET    /api/recycling/my/list            - 我的申请列表
 *   GET    /api/recycling/detail/:id         - 申请详情
 *   POST   /api/recycling/confirm/:id        - 确认报价
 *   POST   /api/recycling/reject-quote/:id   - 拒绝报价
 *   POST   /api/recycling/cancel/:id         - 取消申请
 */
@RestController
@RequestMapping("/api/recycling")
@RequiredArgsConstructor
public class RecyclingController {

    private final RecyclingService recyclingService;

    /**
     * 1. 提交回收申请
     * POST /api/recycling/submit
     */
    @PostMapping("/submit")
    public Result<RecyclingVO> submit(@Validated @RequestBody RecyclingApplyDTO dto) {
        Long userId = UserContext.getUserId();
        RecyclingVO vo = recyclingService.submitApply(userId, dto);
        return Result.success(vo);
    }

    /**
     * 2. 我的申请列表（分页）
     * GET /api/recycling/my/list
     */
    @GetMapping("/my/list")
    public Result<Page<RecyclingVO>> myList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<RecyclingVO> page = recyclingService.getMyList(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 3. 申请详情
     * GET /api/recycling/detail/:id
     */
    @GetMapping("/detail/{id}")
    public Result<RecyclingVO> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        RecyclingVO vo = recyclingService.getDetail(userId, id);
        return Result.success(vo);
    }

    /**
     * 4. 确认报价
     * POST /api/recycling/confirm/:id
     */
    @PostMapping("/confirm/{id}")
    public Result<Void> confirm(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        recyclingService.confirmQuote(userId, id);
        return Result.success();
    }

    /**
     * 5. 拒绝报价
     * POST /api/recycling/reject-quote/:id
     */
    @PostMapping("/reject-quote/{id}")
    public Result<Void> rejectQuote(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        recyclingService.rejectQuote(userId, id);
        return Result.success();
    }

    /**
     * 6. 取消申请
     * POST /api/recycling/cancel/:id
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        recyclingService.cancelApply(userId, id);
        return Result.success();
    }
}
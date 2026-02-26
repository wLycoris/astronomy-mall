package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.RefundAuditDTO;
import com.astronomy.mall.module.admin.dto.RefundQueryDTO;
import com.astronomy.mall.module.admin.service.AdminRefundService;
import com.astronomy.mall.module.admin.vo.AdminRefundDetailVO;
import com.astronomy.mall.module.admin.vo.AdminRefundVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 后台退款审核管理Controller
 *
 * 文件路径: com.astronomy.mall.module.admin.controller.AdminRefundController
 * 权限要求: 管理员 (role=1) —— AdminInterceptor 拦截 /api/admin/**
 */
@Api(tags = "后台-退款审核管理")
@RestController
@RequestMapping("/api/admin/refund")
@RequiredArgsConstructor
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

    /**
     * 退款列表（分页）
     * GET /api/admin/refund/list
     */
    @ApiOperation("退款列表")
    @GetMapping("/list")
    public Result<Page<AdminRefundVO>> getRefundList(RefundQueryDTO queryDTO) {
        Page<AdminRefundVO> page = adminRefundService.getRefundList(queryDTO);
        return Result.success(page);
    }

    /**
     * 退款详情
     * GET /api/admin/refund/detail/:id
     */
    @ApiOperation("退款详情")
    @GetMapping("/detail/{id}")
    public Result<AdminRefundDetailVO> getRefundDetail(
            @ApiParam("退款ID") @PathVariable Long id) {
        return Result.success(adminRefundService.getRefundDetail(id));
    }

    /**
     * 审核通过
     * POST /api/admin/refund/approve/:id
     */
    @ApiOperation("审核通过")
    @PostMapping("/approve/{id}")
    @AdminLog("退款审核通过")
    public Result<Void> approveRefund(
            @ApiParam("退款ID") @PathVariable Long id,
            @Valid @RequestBody RefundAuditDTO auditDTO) {
        adminRefundService.approveRefund(id, auditDTO);
        return Result.success();
    }

    /**
     * 审核拒绝
     * POST /api/admin/refund/reject/:id
     */
    @ApiOperation("审核拒绝")
    @PostMapping("/reject/{id}")
    @AdminLog("退款审核拒绝")
    public Result<Void> rejectRefund(
            @ApiParam("退款ID") @PathVariable Long id,
            @Valid @RequestBody RefundAuditDTO auditDTO) {
        adminRefundService.rejectRefund(id, auditDTO);
        return Result.success();
    }

    /**
     * 处理退款（手动重试）
     * POST /api/admin/refund/process/:id
     */
    @ApiOperation("处理退款")
    @PostMapping("/process/{id}")
    @AdminLog("手动处理退款")
    public Result<Void> processRefund(
            @ApiParam("退款ID") @PathVariable Long id) {
        adminRefundService.processRefund(id);
        return Result.success();
    }
}
package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.InstallationAdminCancelDTO;
import com.astronomy.mall.module.admin.dto.InstallationConfirmDTO;
import com.astronomy.mall.module.admin.dto.InstallationQueryDTO;
import com.astronomy.mall.module.admin.service.AdminInstallationService;
import com.astronomy.mall.module.admin.vo.AdminInstallationVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员安装预约 Controller
 *
 * 📌 文件路径:
 *   module/admin/controller/AdminInstallationController.java
 *
 * 📌 接口列表（3个）:
 *   GET  /api/admin/installation/list          - 预约列表（分页+筛选）
 *   POST /api/admin/installation/confirm/{id}  - 确认预约
 *   POST /api/admin/installation/cancel/{id}   - 取消预约
 *
 * 📌 权限说明:
 *   所有接口需管理员权限（AdminInterceptor 已拦截 /api/admin/**）
 *   adminId 从 request.getAttribute("userId") 获取
 */
@Api(tags = "安装预约管理（管理员端）")
@RestController
@RequestMapping("/api/admin/installation")
@RequiredArgsConstructor
public class AdminInstallationController {

    private final AdminInstallationService adminInstallationService;

    /**
     * 预约列表查询（分页）
     * GET /api/admin/installation/list
     *
     * 支持筛选: 状态(status)、时间范围(startTime/endTime)
     */
    @ApiOperation("预约列表查询")
    @GetMapping("/list")
    public Result<IPage<AdminInstallationVO>> list(InstallationQueryDTO dto) {
        IPage<AdminInstallationVO> page = adminInstallationService.getList(dto);
        return Result.success(page);
    }

    /**
     * 确认安装预约
     * POST /api/admin/installation/confirm/{id}
     *
     * 📌 确认后自动发送通知给用户（MALL_INSTALLATION_CONFIRMED）
     */
    @ApiOperation("确认安装预约（填写工程师信息）")
    @AdminLog("确认安装预约")
    @PostMapping("/confirm/{id}")
    public Result<?> confirm(@PathVariable Long id,
                             @Validated @RequestBody InstallationConfirmDTO dto,
                             HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminInstallationService.confirmInstallation(adminId, id, dto);
        return Result.success("预约已确认，通知已发送给用户");
    }

    /**
     * 取消安装预约（管理员端）
     * POST /api/admin/installation/cancel/{id}
     */
    @ApiOperation("取消安装预约（管理员）")
    @AdminLog("取消安装预约")
    @PostMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id,
                            @Validated @RequestBody InstallationAdminCancelDTO dto,
                            HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminInstallationService.cancelInstallation(adminId, id, dto);
        return Result.success("预约已取消");
    }
}
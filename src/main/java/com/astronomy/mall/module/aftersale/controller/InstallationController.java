package com.astronomy.mall.module.aftersale.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.aftersale.dto.InstallationApplyDTO;
import com.astronomy.mall.module.aftersale.service.InstallationService;
import com.astronomy.mall.module.aftersale.vo.InstallationVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 安装预约 Controller（用户端）
 *
 * 📌 文件路径:
 *   module/aftersale/controller/InstallationController.java
 *
 * 📌 接口列表（3个）:
 *   POST   /api/installation/submit        - 提交安装预约
 *   GET    /api/installation/my/list       - 我的预约列表
 *   POST   /api/installation/cancel/{id}   - 取消预约
 *
 * 📌 认证说明:
 *   所有接口均需登录（JwtInterceptor 已拦截 /api/**）
 *   userId 从 request.getAttribute("userId") 获取
 */
@Api(tags = "安装预约（用户端）")
@RestController
@RequestMapping("/api/installation")
@RequiredArgsConstructor
public class InstallationController {

    private final InstallationService installationService;

    /**
     * 提交安装预约
     * POST /api/installation/submit
     *
     * 📌 前置校验（Service层执行）:
     *   1. 订单归属当前用户
     *   2. 订单状态 = 2(待收货) 或 3(已完成)
     *   3. 同一订单不能重复预约
     *   4. productId 必须属于该订单
     */
    @ApiOperation("提交安装预约")
    @PostMapping("/submit")
    public Result<?> submit(@Validated @RequestBody InstallationApplyDTO dto,
                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        installationService.submitInstallation(userId, dto);
        return Result.success("预约提交成功，等待管理员确认");
    }

    /**
     * 查询我的预约列表
     * GET /api/installation/my/list?pageNum=1&pageSize=10
     */
    @ApiOperation("查询我的预约列表")
    @GetMapping("/my/list")
    public Result<IPage<InstallationVO>> myList(
            @ApiParam("页码，默认1")     @RequestParam(defaultValue = "1")  Integer pageNum,
            @ApiParam("每页数量，默认10") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<InstallationVO> page = installationService.getMyList(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 取消安装预约（用户端）
     * POST /api/installation/cancel/{id}
     *
     * 📌 仅可取消状态=0(待确认)的预约
     */
    @ApiOperation("取消安装预约")
    @PostMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        installationService.cancelInstallation(userId, id);
        return Result.success("预约已取消");
    }
}
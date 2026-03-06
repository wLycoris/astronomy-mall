package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.service.AdminSettingService;
import com.astronomy.mall.module.admin.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统设置 Controller
 *
 * 📌 接口列表 (共 12 个):
 *
 *   基础设置:
 *     GET  /api/admin/setting/basic         - 获取基础设置
 *     PUT  /api/admin/setting/basic         - 更新基础设置
 *
 *   运费设置:
 *     GET  /api/admin/setting/freight       - 获取运费设置
 *     PUT  /api/admin/setting/freight       - 更新运费设置
 *
 *   支付设置:
 *     GET  /api/admin/setting/payment       - 获取支付设置
 *     PUT  /api/admin/setting/payment       - 更新支付设置
 *
 *   SEO设置:
 *     GET  /api/admin/setting/seo           - 获取SEO设置
 *     PUT  /api/admin/setting/seo           - 更新SEO设置
 *
 *   注册设置:
 *     GET  /api/admin/setting/register      - 获取注册设置
 *     PUT  /api/admin/setting/register      - 更新注册设置
 *
 *   维护模式:
 *     GET  /api/admin/setting/maintenance   - 获取维护模式设置
 *     PUT  /api/admin/setting/maintenance   - 更新维护模式设置
 *
 * 📌 权限: 全部接口需要管理员权限 (AdminInterceptor 拦截)
 */
@Api(tags = "后台-系统设置管理")
@RestController
@RequestMapping("/api/admin/setting")
@RequiredArgsConstructor
public class AdminSettingController {

    private final AdminSettingService adminSettingService;

    // ========================================================
    //  基础设置
    // ========================================================

    /**
     * 获取基础设置
     * 包含: 商城名称/Logo/简介/联系方式/备案号/版权信息
     */
    @ApiOperation("获取基础设置")
    @GetMapping("/basic")
    public Result<BasicSettingVO> getBasicSetting() {
        return Result.success(adminSettingService.getBasicSetting());
    }

    /**
     * 更新基础设置
     * @AdminLog 记录操作日志
     */
    @ApiOperation("更新基础设置")
    @AdminLog("更新基础设置")
    @PutMapping("/basic")
    public Result<Void> updateBasicSetting(@RequestBody @Validated BasicSettingDTO dto) {
        adminSettingService.updateBasicSetting(dto);
        return Result.success();
    }

    // ========================================================
    //  运费设置
    // ========================================================

    /**
     * 获取运费设置
     * 包含: 默认运费/包邮开关/包邮金额
     */
    @ApiOperation("获取运费设置")
    @GetMapping("/freight")
    public Result<FreightSettingVO> getFreightSetting() {
        return Result.success(adminSettingService.getFreightSetting());
    }

    /**
     * 更新运费设置
     */
    @ApiOperation("更新运费设置")
    @AdminLog("更新运费设置")
    @PutMapping("/freight")
    public Result<Void> updateFreightSetting(@RequestBody @Validated FreightSettingDTO dto) {
        adminSettingService.updateFreightSetting(dto);
        return Result.success();
    }

    // ========================================================
    //  支付设置
    // ========================================================

    /**
     * 获取支付设置
     * 包含: 支付方式开关(支付宝/微信/余额)/超时时间/自动确认收货天数
     */
    @ApiOperation("获取支付设置")
    @GetMapping("/payment")
    public Result<PaymentSettingVO> getPaymentSetting() {
        return Result.success(adminSettingService.getPaymentSetting());
    }

    /**
     * 更新支付设置
     * 业务校验: 至少需要开启一种支付方式
     */
    @ApiOperation("更新支付设置")
    @AdminLog("更新支付设置")
    @PutMapping("/payment")
    public Result<Void> updatePaymentSetting(@RequestBody @Validated PaymentSettingDTO dto) {
        adminSettingService.updatePaymentSetting(dto);
        return Result.success();
    }

    // ========================================================
    //  SEO 设置
    // ========================================================

    /**
     * 获取 SEO 设置
     * 包含: 网站标题/关键词/描述
     */
    @ApiOperation("获取SEO设置")
    @GetMapping("/seo")
    public Result<SeoSettingVO> getSeoSetting() {
        return Result.success(adminSettingService.getSeoSetting());
    }

    /**
     * 更新 SEO 设置
     */
    @ApiOperation("更新SEO设置")
    @AdminLog("更新SEO设置")
    @PutMapping("/seo")
    public Result<Void> updateSeoSetting(@RequestBody @Validated SeoSettingDTO dto) {
        adminSettingService.updateSeoSetting(dto);
        return Result.success();
    }

    // ========================================================
    //  注册设置
    // ========================================================

    /**
     * 获取注册设置
     * 包含: 是否开放注册/邮箱验证/仅限邀请/默认头像
     */
    @ApiOperation("获取注册设置")
    @GetMapping("/register")
    public Result<RegisterSettingVO> getRegisterSetting() {
        return Result.success(adminSettingService.getRegisterSetting());
    }

    /**
     * 更新注册设置
     */
    @ApiOperation("更新注册设置")
    @AdminLog("更新注册设置")
    @PutMapping("/register")
    public Result<Void> updateRegisterSetting(@RequestBody @Validated RegisterSettingDTO dto) {
        adminSettingService.updateRegisterSetting(dto);
        return Result.success();
    }

    // ========================================================
    //  维护模式
    // ========================================================

    /**
     * 获取维护模式设置
     */
    @ApiOperation("获取维护模式设置")
    @GetMapping("/maintenance")
    public Result<MaintenanceSettingVO> getMaintenanceSetting() {
        return Result.success(adminSettingService.getMaintenanceSetting());
    }

    /**
     * 更新维护模式设置
     * ⚠️ 开启维护模式后，普通用户将无法访问商城前台
     */
    @ApiOperation("更新维护模式设置")
    @AdminLog("更新维护模式")
    @PutMapping("/maintenance")
    public Result<Void> updateMaintenanceSetting(@RequestBody @Validated MaintenanceSettingDTO dto) {
        adminSettingService.updateMaintenanceSetting(dto);
        return Result.success();
    }
}
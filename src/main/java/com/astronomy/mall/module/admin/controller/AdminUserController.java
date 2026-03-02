package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.UserQueryDTO;
import com.astronomy.mall.module.admin.dto.UserRoleDTO;
import com.astronomy.mall.module.admin.dto.UserStatusDTO;
import com.astronomy.mall.module.admin.service.AdminUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 后台用户管理 Controller
 *
 * <p>提供以下接口：</p>
 * <pre>
 * GET    /api/admin/user/list          - 用户列表（分页 + 搜索 + 筛选）
 * GET    /api/admin/user/detail/{id}   - 用户详情
 * POST   /api/admin/user/status/{id}   - 修改用户状态（禁用 / 启用）
 * PUT    /api/admin/user/role/{id}     - 设置用户角色
 * </pre>
 *
 * <p>所有接口均需要管理员权限（由 AdminInterceptor 拦截校验）。</p>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
@Api(tags = "后台管理 - 用户管理")
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    /**
     * 用户列表（分页）
     *
     * <p>支持按关键词（用户名/昵称/手机/邮箱）、角色、状态、观测等级、注册时间范围筛选。</p>
     *
     * @param dto 查询参数
     * @return 分页用户列表
     */
    @GetMapping("/list")
    @ApiOperation("用户列表（分页）")
    public Result<IPage<?>> getUserList(UserQueryDTO dto) {
        return Result.success(adminUserService.getUserList(dto));
    }

    /**
     * 用户详情
     *
     * <p>返回用户基本信息、消费统计、近期5条订单、近期5条登录日志。</p>
     *
     * @param id 用户ID
     * @return AdminUserDetailVO
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("用户详情")
    public Result<?> getUserDetail(
            @ApiParam("用户ID") @PathVariable Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    /**
     * 修改用户状态（禁用 / 启用）
     *
     * <p>状态变更会记录到管理员操作日志（@AdminLog）。</p>
     *
     * @param id  用户ID
     * @param dto 目标状态及原因
     * @return 操作结果
     */
    @PostMapping("/status/{id}")
    @ApiOperation("修改用户状态（禁用 / 启用）")
    @AdminLog("修改用户状态")
    public Result<Void> updateUserStatus(
            @ApiParam("用户ID") @PathVariable Long id,
            @RequestBody @Valid UserStatusDTO dto) {
        adminUserService.updateUserStatus(id, dto);
        return Result.success();
    }

    /**
     * 设置用户角色（普通用户 ↔ 管理员）
     *
     * <p>角色变更会记录到管理员操作日志（@AdminLog）。</p>
     *
     * @param id  用户ID
     * @param dto 目标角色
     * @return 操作结果
     */
    @PutMapping("/role/{id}")
    @ApiOperation("设置用户角色")
    @AdminLog("设置用户角色")
    public Result<Void> updateUserRole(
            @ApiParam("用户ID") @PathVariable Long id,
            @RequestBody @Valid UserRoleDTO dto) {
        adminUserService.updateUserRole(id, dto);
        return Result.success();
    }
}
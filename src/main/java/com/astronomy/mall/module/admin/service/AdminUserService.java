package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.UserQueryDTO;
import com.astronomy.mall.module.admin.dto.UserRoleDTO;
import com.astronomy.mall.module.admin.dto.UserStatusDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 后台用户管理 Service 接口
 *
 * <p>提供以下功能：</p>
 * <ul>
 *   <li>用户列表分页查询（支持多维度搜索/筛选）</li>
 *   <li>用户详情查看（基本信息 + 消费统计 + 近期订单 + 登录日志）</li>
 *   <li>用户状态管理（禁用 / 启用）</li>
 *   <li>用户角色管理（普通用户 ↔ 管理员）</li>
 * </ul>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
public interface AdminUserService {

    /**
     * 用户列表分页查询
     *
     * @param dto 查询条件（关键词 / 角色 / 状态 / 等级 / 时间范围 / 分页）
     * @return 分页结果
     */
    IPage<?> getUserList(UserQueryDTO dto);

    /**
     * 用户详情
     *
     * @param id 用户ID
     * @return AdminUserDetailVO（基本信息 + 消费统计 + 近期订单 + 登录日志）
     */
    Object getUserDetail(Long id);

    /**
     * 修改用户状态（禁用 / 启用）
     *
     * @param id  用户ID
     * @param dto 目标状态及操作原因
     */
    void updateUserStatus(Long id, UserStatusDTO dto);

    /**
     * 设置用户角色（普通用户 ↔ 管理员）
     *
     * @param id  用户ID
     * @param dto 目标角色
     */
    void updateUserRole(Long id, UserRoleDTO dto);
}
package com.astronomy.mall.module.admin.dto;

import lombok.Data;

/**
 * 后台用户列表查询 DTO
 *
 * <p>用于后台管理员查询用户列表时的入参封装，支持多维度筛选。</p>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
@Data
public class UserQueryDTO {

    /**
     * 搜索关键词
     * <p>模糊匹配：用户名 / 昵称 / 手机号 / 邮箱</p>
     */
    private String keyword;

    /**
     * 角色筛选
     * <ul>
     *   <li>0 - 普通用户</li>
     *   <li>1 - 管理员</li>
     * </ul>
     */
    private Integer role;

    /**
     * 状态筛选
     * <ul>
     *   <li>0 - 禁用</li>
     *   <li>1 - 启用</li>
     * </ul>
     */
    private Integer status;

    /**
     * 观测等级筛选
     * <ul>
     *   <li>1 - 入门</li>
     *   <li>2 - 初级</li>
     *   <li>3 - 中级</li>
     *   <li>4 - 高级</li>
     *   <li>5 - 专家</li>
     * </ul>
     */
    private Integer observationLevel;

    /**
     * 注册时间 - 开始日期
     * <p>格式：yyyy-MM-dd</p>
     */
    private String startTime;

    /**
     * 注册时间 - 结束日期
     * <p>格式：yyyy-MM-dd，查询时自动补全为当天 23:59:59</p>
     */
    private String endTime;

    /**
     * 当前页码，默认第 1 页
     */
    private Integer page = 1;

    /**
     * 每页条数，默认 10 条
     */
    private Integer size = 10;
}
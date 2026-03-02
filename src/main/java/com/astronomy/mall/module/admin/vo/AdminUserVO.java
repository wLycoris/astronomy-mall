package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台用户列表 VO
 *
 * <p>返回用户列表分页数据，包含基本信息和订单统计。</p>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
@Data
public class AdminUserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /**
     * 角色
     * <ul>
     *   <li>0 - 普通用户</li>
     *   <li>1 - 管理员</li>
     * </ul>
     */
    private Integer role;

    /** 角色名称（中文展示用） */
    private String roleName;

    /**
     * 状态
     * <ul>
     *   <li>0 - 禁用</li>
     *   <li>1 - 启用</li>
     * </ul>
     */
    private Integer status;

    /** 状态名称（中文展示用） */
    private String statusName;

    /**
     * 观测等级
     * <ul>
     *   <li>1 - 入门 / 2 - 初级 / 3 - 中级 / 4 - 高级 / 5 - 专家</li>
     * </ul>
     */
    private Integer observationLevel;

    /** 观测等级名称（中文展示用） */
    private String observationLevelName;

    /** 所在城市 */
    private String city;

    /** 兴趣标签（JSON数组字符串，如 ["行星观测","深空摄影"]） */
    private String interestTags;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /** 订单总数 */
    private Long orderCount;

    /** 消费总金额（已完成订单汇总） */
    private BigDecimal totalAmount;
}
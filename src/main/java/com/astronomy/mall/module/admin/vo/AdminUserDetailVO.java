package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 后台用户详情 VO
 *
 * <p>返回单个用户的详细信息，包含：基本信息、消费统计、近期订单、近期登录日志。</p>
 *
 * @author astronomy-mall
 * @since 2026-03-02
 */
@Data
public class AdminUserDetailVO {

    // ==================== 基本信息 ====================

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

    /** 所在省份 */
    private String province;

    /** 兴趣标签（JSON数组字符串） */
    private String interestTags;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    // ==================== 消费统计 ====================

    /** 订单总数（所有状态） */
    private Long orderCount;

    /** 已完成订单数（status=3） */
    private Long completedOrderCount;

    /**
     * 消费总金额
     * <p>仅统计已完成订单（status=3）的 total_amount 汇总</p>
     */
    private BigDecimal totalAmount;

    /** 退款次数 */
    private Long refundCount;

    /** 评价次数（未被删除的） */
    private Long reviewCount;

    // ==================== 近期订单（最近5条） ====================

    /**
     * 近期订单列表
     * <p>每条包含字段：id / order_no / total_amount / status / statusName / create_time</p>
     */
    private List<Map<String, Object>> recentOrders;

    // ==================== 近期登录日志（最近5条） ====================

    /**
     * 近期登录日志列表
     * <p>每条包含字段：id / login_time / ip_address / device / status</p>
     */
    private List<Map<String, Object>> loginLogs;
}
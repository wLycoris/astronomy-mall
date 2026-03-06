package com.astronomy.mall.module.user.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人中心 - 概览页聚合VO
 *
 * 📌 对应接口: GET /api/user/overview
 * 📌 一次请求返回所有概览所需数据，避免前端多次请求
 *
 * 数据来源:
 * - 用户信息: tb_user
 * - 订单统计: tb_order (按状态分组 COUNT)
 * - 钱包余额: tb_user.balance
 * - 最近流水: tb_balance_log (最新一条)
 */
@Data
public class UserOverviewVO {

    // ──────────────────────────────────────────
    // 用户基本信息
    // ──────────────────────────────────────────

    /** 用户ID */
    private Long userId;

    /** 昵称（优先展示，为空时展示用户名） */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 所在城市 */
    private String city;

    /** 观测等级 (1-入门 2-初级 3-中级 4-高级 5-专家) */
    private Integer observationLevel;

    /** 观测等级文字描述 */
    private String observationLevelText;

    /** 注册时间 */
    private LocalDateTime createTime;

    /**
     * 兴趣标签列表（解析自 tb_user.interest_tags JSON数组）
     * 示例: ["行星观测", "深空摄影"]
     */
    private List<String> interestTags;

    // ──────────────────────────────────────────
    // 订单状态格子（点击可跳转对应状态订单列表）
    // ──────────────────────────────────────────

    /** 待付款订单数 (status=0) */
    private Integer pendingPayCount;

    /** 待发货订单数 (status=1) */
    private Integer pendingShipCount;

    /** 待收货订单数 (status=2) */
    private Integer pendingReceiveCount;

    /** 待评价订单数 (status=3 且未评价) */
    private Integer pendingReviewCount;

    /** 退款/售后订单数 (有进行中的退款申请) */
    private Integer refundingCount;

    // ──────────────────────────────────────────
    // 右上角三格统计
    // ──────────────────────────────────────────

    /** 累计订单总数（所有状态含已取消） */
    private Integer totalOrders;

    /** 累计消费金额（仅统计已完成订单 status=3 的实付金额） */
    private BigDecimal totalSpent;

    /** 已发布评价数（status=1 正常评价） */
    private Integer totalReviews;

    // ──────────────────────────────────────────
    // 钱包信息
    // ──────────────────────────────────────────

    /** 钱包余额 */
    private BigDecimal balance;

    /** 最近一笔流水备注（如 "二手回收到账"） */
    private String lastLogRemark;

    /** 最近一笔流水金额（正数=收入 负数=支出） */
    private BigDecimal lastLogAmount;

    /** 最近一笔流水时间 */
    private LocalDateTime lastLogTime;

    /** 最近一笔流水类型(1-充值 2-提现 3-回收入账 4-购物扣款) */
    private Integer lastLogType;
}
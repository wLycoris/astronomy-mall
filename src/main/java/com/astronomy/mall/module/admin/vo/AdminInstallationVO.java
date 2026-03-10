package com.astronomy.mall.module.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员查看安装预约 VO
 *
 * 📌 文件路径:
 *   module/admin/vo/AdminInstallationVO.java
 *
 * 相比用户端 InstallationVO，增加了:
 *   - username / nickname (用户信息)
 *   - adminId (操作管理员ID)
 *   - updateTime (更新时间)
 */
@Data
public class AdminInstallationVO {

    /** 预约ID */
    private Long id;

    // ── 用户信息 ──────────────────────────────
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 昵称 */
    private String nickname;

    // ── 订单信息 ──────────────────────────────
    /** 关联订单ID */
    private Long orderId;
    /** 订单号 */
    private String orderNo;

    // ── 商品信息 ──────────────────────────────
    /** 关联商品ID */
    private Long productId;
    /** 商品名称（快照） */
    private String productName;

    // ── 安装信息 ──────────────────────────────
    /** 安装地址 */
    private String address;
    /** 联系人 */
    private String contactName;
    /** 联系电话 */
    private String contactPhone;
    /** 期望上门时间 */
    private LocalDateTime expectedTime;
    /** 确认上门时间 */
    private LocalDateTime confirmedTime;
    /** 工程师姓名 */
    private String engineerName;
    /** 工程师联系方式 */
    private String engineerPhone;

    // ── 备注 ──────────────────────────────────
    /** 用户备注 */
    private String userRemark;
    /** 管理员备注/取消原因 */
    private String adminRemark;

    // ── 状态 ──────────────────────────────────
    /** 状态(0-待确认 1-已确认 2-已取消) */
    private Integer status;

    /** 操作管理员ID */
    private Long adminId;

    /** 预约提交时间 */
    private LocalDateTime createTime;

    /** 最近更新时间 */
    private LocalDateTime updateTime;

    /**
     * 状态文字（后端计算，减少前端逻辑）
     */
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "已取消";
            default: return "未知";
        }
    }
}
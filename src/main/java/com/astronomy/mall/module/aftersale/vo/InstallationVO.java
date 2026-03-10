package com.astronomy.mall.module.aftersale.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安装预约 VO（用户端视图）
 *
 * 状态文字映射:
 *   0 → 待确认
 *   1 → 已确认
 *   2 → 已取消
 */
@Data
public class InstallationVO {

    /** 预约ID */
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 订单号（从 tb_order.order_no 关联） */
    private String orderNo;

    /** 关联商品ID */
    private Long productId;

    /** 商品名称（快照） */
    private String productName;

    /** 安装地址 */
    private String address;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 期望上门时间 */
    private LocalDateTime expectedTime;

    /** 确认上门时间（已确认后有值） */
    private LocalDateTime confirmedTime;

    /** 工程师姓名（已确认后有值） */
    private String engineerName;

    /** 工程师联系方式（已确认后有值） */
    private String engineerPhone;

    /** 用户备注 */
    private String userRemark;

    /** 管理员备注/取消原因 */
    private String adminRemark;

    /** 状态(0-待确认 1-已确认 2-已取消) */
    private Integer status;

    /** 状态文字（前端展示用） */
    private String statusText;

    /** 预约提交时间 */
    private LocalDateTime createTime;

    /**
     * 根据 status 字段返回状态文字
     * 在 Service 层设置，减少前端逻辑
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

    /** 是否可以取消（仅状态=0时允许） */
    public boolean getCanCancel() {
        return Integer.valueOf(0).equals(status);
    }
}
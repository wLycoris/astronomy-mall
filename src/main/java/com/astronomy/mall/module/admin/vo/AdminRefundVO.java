package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台退款列表VO
 */
@Data
public class AdminRefundVO {

    /** 退款ID */
    private Long id;

    /** 退款单号 */
    private String refundNo;

    /** 支付ID */
    private Long paymentId;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /**
     * 退款类型
     * 1-仅退款 2-退货退款
     */
    private Integer refundType;

    /** 退款类型描述 */
    private String refundTypeDesc;

    /**
     * 退款状态
     * 0-待审核 1-审核通过 2-审核拒绝 3-退款成功 4-退款失败
     */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 审核管理员ID */
    private Long adminId;

    /** 审核管理员姓名 */
    private String adminName;

    /** 审核备注 */
    private String adminRemark;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 退款成功时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
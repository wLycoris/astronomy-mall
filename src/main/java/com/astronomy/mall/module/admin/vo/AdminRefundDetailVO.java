package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台退款详情VO（包含完整订单信息）
 */
@Data
public class AdminRefundDetailVO {

    // ===== 退款信息 =====
    /** 退款ID */
    private Long id;

    /** 退款单号 */
    private String refundNo;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 退款类型(1-仅退款 2-退货退款) */
    private Integer refundType;

    /** 退款类型描述 */
    private String refundTypeDesc;

    /** 退款状态 */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

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

    // ===== 用户信息 =====
    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    // ===== 订单信息 =====
    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 订单总金额 */
    private BigDecimal orderAmount;

    /** 收货人 */
    private String receiverName;

    /** 收货手机 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 订单状态 */
    private Integer orderStatus;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderCreateTime;

    // ===== 支付信息 =====
    /** 支付ID */
    private Long paymentId;

    /** 支付流水号 */
    private String paymentNo;

    /** 支付方式(1-支付宝 2-微信 3-余额) */
    private Integer paymentType;

    /** 支付方式描述 */
    private String paymentTypeDesc;

    /** 支付金额 */
    private BigDecimal paymentAmount;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    // ===== 订单商品列表 =====
    /** 订单商品列表 */
    private List<OrderItemVO> orderItems;

    @Data
    public static class OrderItemVO {
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal productPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
    }
}
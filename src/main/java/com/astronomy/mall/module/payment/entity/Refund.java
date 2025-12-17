package com.astronomy.mall.module.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 */
@Data
@TableName("tb_refund")
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 退款类型(1-仅退款 2-退货退款) */
    private Integer refundType;

    /** 状态(0-待审核 1-审核通过 2-审核拒绝 3-退款成功 4-退款失败) */
    private Integer status;

    /** 审核管理员ID */
    private Long adminId;

    /** 审核备注 */
    private String adminRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 退款成功时间 */
    private LocalDateTime refundTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
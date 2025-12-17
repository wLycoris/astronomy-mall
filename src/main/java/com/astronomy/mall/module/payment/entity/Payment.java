package com.astronomy.mall.module.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@TableName("tb_payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付流水号 */
    private String paymentNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 支付方式(1-支付宝 2-微信 3-余额) */
    private Integer paymentType;

    /** 支付金额 */
    private BigDecimal paymentAmount;

    /** 状态(0-待支付 1-支付成功 2-支付失败 3-已退款) */
    private Integer status;

    /** 支付时间 */
    private LocalDateTime paymentTime;

    /** 第三方交易流水号 */
    private String transactionId;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.astronomy.mall.module.payment.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录VO
 */
@Data
public class RefundVO {

    private Long id;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private BigDecimal refundAmount;
    private String refundReason;
    private Integer refundType;
    private String refundTypeName;
    private Integer status;
    private String statusName;
    private String adminRemark;
    private LocalDateTime auditTime;
    private LocalDateTime refundTime;
    private LocalDateTime createTime;

    public String getRefundTypeName() {
        if (refundType == null) return "未知";
        return refundType == 1 ? "仅退款" : "退货退款";
    }

    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "审核通过";
            case 2: return "审核拒绝";
            case 3: return "退款成功";
            case 4: return "退款失败";
            default: return "未知";
        }
    }
}
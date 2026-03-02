package com.astronomy.mall.module.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单统计 VO
 *
 * 接口: GET /api/admin/statistics/order-status
 */
@Data
public class OrderStatisticsVO {

    // ===== 订单状态分布 =====
    /** 待支付订单数 */
    private Integer waitPayCount;
    /** 待发货订单数 */
    private Integer waitDeliverCount;
    /** 待收货订单数 */
    private Integer waitReceiveCount;
    /** 已完成订单数 */
    private Integer completedCount;
    /** 已取消订单数 */
    private Integer cancelledCount;

    // ===== 订单金额分布 (区间分布) =====
    /** 金额分布区间列表 */
    private List<AmountRangeVO> amountDistribution;

    @Data
    public static class AmountRangeVO {
        /** 区间标签 (如: "0-100元", "100-500元") */
        private String label;
        /** 该区间订单数 */
        private Integer count;
        /** 占比百分比 */
        private Double percentage;
    }
}
package com.astronomy.mall.module.admin.dto;

import lombok.Data;

/**
 * 退款查询DTO
 * 接口: GET /api/admin/refund/list
 */
@Data
public class RefundQueryDTO {

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页数量 */
    private Integer pageSize = 10;

    /**
     * 退款状态
     * 0-待审核 1-审核通过 2-审核拒绝 3-退款成功 4-退款失败
     */
    private Integer status;

    /** 订单编号 */
    private String orderNo;

    /** 退款单号 */
    private String refundNo;

    /** 用户ID */
    private Long userId;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;
}
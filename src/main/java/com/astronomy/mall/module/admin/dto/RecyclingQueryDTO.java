package com.astronomy.mall.module.admin.dto;

import lombok.Data;

/**
 * 管理员端 - 回收申请列表查询 DTO
 *
 * 📌 接口: GET /api/admin/recycling/list
 */
@Data
public class RecyclingQueryDTO {

    /** 页码（默认1） */
    private Integer pageNum = 1;

    /** 每页数量（默认10） */
    private Integer pageSize = 10;

    /**
     * 状态筛选（为 null 时查询所有状态）
     * 0-待审核 1-已报价 2-用户确认 3-待取件 4-已回收 5-已拒绝 6-用户取消
     */
    private Integer status;

    /** 器材名称（模糊搜索） */
    private String productName;

    /** 回收单号（精确搜索） */
    private String recycleNo;

    /** 用户ID（筛选指定用户） */
    private Long userId;

    /** 查询开始时间 */
    private String startTime;

    /** 查询结束时间 */
    private String endTime;
}
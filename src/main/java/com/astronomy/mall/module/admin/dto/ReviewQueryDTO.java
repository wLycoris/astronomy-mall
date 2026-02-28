package com.astronomy.mall.module.admin.dto;

import lombok.Data;

/**
 * 评价列表查询 DTO
 * 接口: GET /api/admin/review/list
 *
 * 修复记录 2026-02-26:
 * - isTop 从 Boolean 改为 Integer，避免 MyBatis OGNL 判断 Boolean 不可靠
 *   前端传 1=只看置顶，不传或传 null=全部
 * - 新增 productName 字段，支持按商品名称模糊搜索
 */
@Data
public class ReviewQueryDTO {

    /** 页码，默认1 */
    private Integer pageNum = 1;

    /** 每页数量，默认10 */
    private Integer pageSize = 10;

    /** 商品ID（精确筛选，一般不用，保留备用） */
    private Long productId;

    /**
     * 商品名称（模糊搜索，前端按商品筛选用这个）
     * 例如传 "望远镜" 会搜索商品名包含"望远镜"的所有评价
     */
    private String productName;

    /** 评分筛选（1-5星，null=全部） */
    private Integer rating;

    /**
     * 评价状态筛选
     * 0 - 已删除
     * 1 - 正常
     * 2 - 待审核
     * null - 全部
     */
    private Integer status;

    /**
     * 置顶筛选
     * 1  - 只看置顶
     * null 或 不传 - 全部
     *
     * 注意: 用 Integer 而非 Boolean，避免 MyBatis OGNL 对 Boolean 判断不稳定
     */
    private Integer isTop;

    /** 关键词（搜索评价内容） */
    private String keyword;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;
}
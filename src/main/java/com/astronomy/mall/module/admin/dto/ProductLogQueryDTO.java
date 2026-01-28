package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品日志查询DTO
 *
 * 路径: com.astronomy.mall.module.admin.dto.ProductLogQueryDTO
 */
@Data
public class ProductLogQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;

    /**
     * 商品ID (可选)
     */
    private Long productId;

    /**
     * 商品名称 (可选,模糊查询)
     */
    private String productName;

    /**
     * 操作类型 (可选: 新增/修改/上架/下架/删除)
     */
    private String operationType;

    /**
     * 操作人ID (可选)
     */
    private Long operatorId;

    /**
     * 操作人姓名 (可选,模糊查询)
     */
    private String operatorName;

    /**
     * 开始时间 (可选)
     */
    private LocalDateTime startTime;

    /**
     * 结束时间 (可选)
     */
    private LocalDateTime endTime;
}
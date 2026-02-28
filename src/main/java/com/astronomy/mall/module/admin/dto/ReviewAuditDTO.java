package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 评价审核 DTO
 * 接口: POST /api/admin/review/audit/:id
 */
@Data
public class ReviewAuditDTO {

    /**
     * 审核操作
     * 1 - 审核通过（状态：2→1）
     * 0 - 删除/拒绝（状态：2→0）
     */
    @NotNull(message = "审核结果不能为空")
    private Integer action;

    /**
     * 审核备注（可选）
     */
    private String remark;
}
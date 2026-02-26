package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 退款审核DTO
 * 接口:
 *   POST /api/admin/refund/approve/:id  - 审核通过
 *   POST /api/admin/refund/reject/:id   - 审核拒绝
 */
@Data
public class RefundAuditDTO {

    /**
     * 审核备注/拒绝原因
     * 审核通过时为可选，审核拒绝时建议填写
     */
    @Size(max = 200, message = "备注不能超过200字符")
    private String adminRemark;
}
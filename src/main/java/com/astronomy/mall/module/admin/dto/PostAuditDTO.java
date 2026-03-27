package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 帖子审核 DTO（管理员用）
 *
 * 对应接口: POST /api/admin/post/audit/{id}
 *
 * 📌 审核操作:
 *   action="approve" → status从1改为2（已发布），发送 POST_APPROVED 通知
 *   action="reject"  → status从1改为3（已拒绝），发送 POST_REJECTED 通知，需填reason
 */
@Data
public class PostAuditDTO {

    /** 审核动作: approve(通过) / reject(拒绝) */
    @NotBlank(message = "审核动作不能为空")
    private String action;

    /** 拒绝原因（action=reject时必填） */
    private String reason;
}

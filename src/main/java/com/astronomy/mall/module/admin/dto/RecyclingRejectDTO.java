package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员端 - 拒绝回收申请 DTO
 *
 * 📌 接口: POST /api/admin/recycling/reject/:id
 */
@Data
public class RecyclingRejectDTO {

    /**
     * 拒绝原因（必填，不超过500字）
     */
    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 500, message = "拒绝原因不超过500字")
    private String adminRemark;
}
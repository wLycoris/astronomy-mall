package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员端 - 安排取件 DTO
 *
 * 📌 接口: POST /api/admin/recycling/arrange/:id
 * 📌 填写快递公司和快递单号后，状态变为【待取件(3)】
 */
@Data
public class RecyclingArrangeDTO {

    /**
     * 快递公司名称（必填，如：顺丰速运、京东快递等）
     */
    @NotBlank(message = "快递公司不能为空")
    @Size(max = 100, message = "快递公司名称不超过100字")
    private String logisticsCompany;

    /**
     * 快递单号（必填）
     */
    @NotBlank(message = "快递单号不能为空")
    @Size(max = 100, message = "快递单号不超过100字")
    private String trackingNumber;
}
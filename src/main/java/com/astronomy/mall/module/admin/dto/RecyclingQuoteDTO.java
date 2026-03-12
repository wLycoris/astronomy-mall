package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 管理员端 - 提交回收报价 DTO
 *
 * 📌 接口: POST /api/admin/recycling/quote/:id
 */
@Data
public class RecyclingQuoteDTO {

    /**
     * 回收报价金额（必填，最小 0.01 元）
     */
    @NotNull(message = "报价金额不能为空")
    @DecimalMin(value = "0.01", message = "报价金额至少 0.01 元")
    private BigDecimal assessedPrice;

    /**
     * 报价备注说明（可选，不超过500字）
     */
    @Size(max = 500, message = "备注不超过500字")
    private String adminRemark;
}
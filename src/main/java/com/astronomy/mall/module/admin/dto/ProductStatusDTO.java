package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 商品上下架DTO
 */
@Data
public class ProductStatusDTO {

    /**
     * 商品ID列表
     */
    @NotEmpty(message = "商品ID不能为空")
    private List<Long> productIds;

    /**
     * 商品状态(0-下架 1-上架)
     */
    @NotNull(message = "商品状态不能为空")
    @Min(value = 0, message = "商品状态只能是0或1")
    @Max(value = 1, message = "商品状态只能是0或1")
    private Integer status;
}
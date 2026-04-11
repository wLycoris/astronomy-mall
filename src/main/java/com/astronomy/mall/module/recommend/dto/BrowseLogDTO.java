package com.astronomy.mall.module.recommend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商品浏览埋点 DTO
 * 前端在 ProductDetail 页面停留时调用 POST /api/recommend/browse
 */
@Data
@ApiModel(description = "商品浏览埋点")
public class BrowseLogDTO {

    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true)
    private Long productId;

    @ApiModelProperty("分类ID(可选,前端有就传)")
    private Long categoryId;

    @ApiModelProperty("来源: detail/list，默认 detail")
    private String source;
}

package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
@ApiModel(description = "商品查询DTO")
public class ProductQueryDTO {

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("关键词搜索(商品名称/品牌)")
    private String keyword;

    @ApiModelProperty("品牌")
    private String brand;

    @ApiModelProperty("最低价格")
    private BigDecimal minPrice;

    @ApiModelProperty("最高价格")
    private BigDecimal maxPrice;

    @ApiModelProperty("是否热卖(0-否 1-是)")
    private Integer isHot;

    @ApiModelProperty("是否新品(0-否 1-是)")
    private Integer isNew;

    @ApiModelProperty("是否推荐(0-否 1-是)")
    private Integer isRecommend;

    @ApiModelProperty("排序字段(price-价格 sales-销量 create_time-上架时间)")
    private String sortField;

    @ApiModelProperty("排序方式(asc-升序 desc-降序)")
    private String sortOrder;

    @ApiModelProperty("页码(默认1)")
    private Integer pageNum = 1;

    @ApiModelProperty("每页数量(默认20)")
    private Integer pageSize = 20;
}
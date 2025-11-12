package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "商品列表返回VO")
public class ProductVO {

    @ApiModelProperty("商品ID")
    private Long id;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("副标题")
    private String subTitle;

    @ApiModelProperty("品牌")
    private String brand;

    @ApiModelProperty("价格")
    private BigDecimal price;

    @ApiModelProperty("原价")
    private BigDecimal originalPrice;

    @ApiModelProperty("库存")
    private Integer stock;

    @ApiModelProperty("销量")
    private Integer sales;

    @ApiModelProperty("主图URL")
    private String mainImage;

    @ApiModelProperty("是否热卖")
    private Integer isHot;

    @ApiModelProperty("是否新品")
    private Integer isNew;

    @ApiModelProperty("是否推荐")
    private Integer isRecommend;

    @ApiModelProperty("评价数量")
    private Integer reviewCount;

    @ApiModelProperty("平均评分")
    private Double avgRating;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
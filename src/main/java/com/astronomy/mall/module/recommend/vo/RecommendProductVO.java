package com.astronomy.mall.module.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 推荐商品 VO
 * 前端推荐卡片展示所需字段
 */
@Data
@ApiModel(description = "推荐商品")
public class RecommendProductVO {

    @ApiModelProperty("商品ID")
    private Long id;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("主图URL")
    private String mainImage;

    @ApiModelProperty("价格")
    private BigDecimal price;

    @ApiModelProperty("原价")
    private BigDecimal originalPrice;

    @ApiModelProperty("销量")
    private Integer sales;

    @ApiModelProperty("推荐理由: 标签匹配/协同过滤/热门推荐/冷启动")
    private String reason;

    @ApiModelProperty("推荐得分(可用于前端排序)")
    private Double score;

    @ApiModelProperty("推荐算法: content/collaborative/hot/coldstart")
    private String algorithm;
}

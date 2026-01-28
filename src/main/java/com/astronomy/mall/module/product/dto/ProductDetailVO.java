package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "商品详情返回VO")
public class ProductDetailVO {

    @ApiModelProperty("商品ID")
    private Long id;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("分类名称")
    private String categoryName;

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

    @ApiModelProperty("商品图片列表")
    private List<String> imageList;

    @ApiModelProperty("商品详情(富文本)")
    private String detail;

    @ApiModelProperty("规格参数(JSON格式)")
    private String specifications;

    @ApiModelProperty("商品标签")
    private String tags;

    @ApiModelProperty("是否热卖")
    private Integer isHot;

    @ApiModelProperty("是否新品")
    private Integer isNew;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;

    @ApiModelProperty("评价数量")
    private Integer reviewCount;

    @ApiModelProperty("平均评分")
    private Double avgRating;

    @ApiModelProperty("好评率")
    private Double goodRate;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
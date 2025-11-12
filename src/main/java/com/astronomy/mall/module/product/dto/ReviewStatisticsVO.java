package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "评价统计VO")
public class ReviewStatisticsVO {

    @ApiModelProperty("评价总数")
    private Integer reviewCount;

    @ApiModelProperty("平均评分")
    private Double avgRating;

    @ApiModelProperty("好评率")
    private Double goodRate;

    @ApiModelProperty("5星评价数")
    private Integer fiveStar;

    @ApiModelProperty("4星评价数")
    private Integer fourStar;

    @ApiModelProperty("3星评价数")
    private Integer threeStar;

    @ApiModelProperty("2星评价数")
    private Integer twoStar;

    @ApiModelProperty("1星评价数")
    private Integer oneStar;
}
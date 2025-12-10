package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评价统计VO
 *
 * 使用场景:
 * - 商品详情页的评价统计展示
 *
 * 统计内容:
 * 1. 基础统计:总数、平均分、好评率、点赞数
 * 2. 星级分布:1-5星各自的数量
 * 3. 有图评价数
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Data
@ApiModel(description = "评价统计VO")
public class ReviewStatisticsVO {

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty(value = "评价总数", example = "128")
    private Integer reviewCount;

    @ApiModelProperty(value = "平均评分", example = "4.6", notes = "精确到小数点后1位")
    private Double avgRating;

    @ApiModelProperty(value = "好评率(%)", example = "92.5", notes = "4-5星评价占比")
    private Double goodRate;

    @ApiModelProperty(value = "总点赞数", example = "356")
    private Integer totalLikes;

    @ApiModelProperty(value = "5星评价数", example = "85")
    private Integer fiveStar;

    @ApiModelProperty(value = "4星评价数", example = "32")
    private Integer fourStar;

    @ApiModelProperty(value = "3星评价数", example = "8")
    private Integer threeStar;

    @ApiModelProperty(value = "2星评价数", example = "2")
    private Integer twoStar;

    @ApiModelProperty(value = "1星评价数", example = "1")
    private Integer oneStar;

    @ApiModelProperty(value = "有图评价数", example = "56")
    private Integer hasImagesCount;
}
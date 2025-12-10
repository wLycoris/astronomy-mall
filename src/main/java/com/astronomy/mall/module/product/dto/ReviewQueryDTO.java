package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 评价查询DTO
 *
 * 使用场景:
 * - 高级版评价列表接口的查询参数
 *
 * 筛选功能:
 * 1. rating: 星级筛选(0=全部,1-5=对应星级)
 * 2. hasImages: 是否有图(0=全部,1=仅看有图)
 * 3. sortType: 排序方式(1-4)
 *
 * 排序方式:
 * - sortType=1: 最新评价(默认)
 * - sortType=2: 点赞最多
 * - sortType=3: 评分最高
 * - sortType=4: 评分最低
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Data
@ApiModel(description = "评价查询DTO")
public class ReviewQueryDTO {

    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Long productId;

    @ApiModelProperty(value = "评分筛选", example = "0", notes = "0=全部,1-5=对应星级", allowableValues = "range[0,5]")
    private Integer rating = 0;

    @ApiModelProperty(value = "排序方式", example = "1", notes = "1=最新,2=点赞最多,3=评分最高,4=评分最低", allowableValues = "range[1,4]")
    private Integer sortType = 1;

    @ApiModelProperty(value = "是否有图", example = "0", notes = "0=全部,1=仅看有图", allowableValues = "range[0,1]")
    private Integer hasImages = 0;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;
}
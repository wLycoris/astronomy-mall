package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 发布评价DTO
 *
 * 使用场景:
 * - 用户发布评价接口的请求体
 *
 * 验证规则:
 * 1. orderId: 必填
 * 2. productId: 必填
 * 3. rating: 必填,1-5星
 * 4. content: 必填,10-500字
 * 5. images: 可选,最多9张
 * 6. isAnonymous: 可选,默认false
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Data
@ApiModel(description = "发布评价DTO")
public class PublishReviewDTO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    private Long orderId;

    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Long productId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    @ApiModelProperty(value = "评分(1-5星)", required = true, example = "5", allowableValues = "range[1,5]")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(min = 10, max = 500, message = "评价内容长度为10-500字")
    @ApiModelProperty(value = "评价内容", required = true, example = "商品质量很好,物流快!")
    private String content;

    @Size(max = 9, message = "最多上传9张图片")
    @ApiModelProperty(value = "评价图片列表", example = "[\"url1\", \"url2\"]", notes = "最多9张")
    private List<String> images;

    @ApiModelProperty(value = "是否匿名", example = "false", notes = "默认false")
    private Boolean isAnonymous = false;
}
package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.*;

/**
 * 商家回复DTO
 *
 * 使用场景:
 * - 商家回复评价接口的请求体
 *
 * 验证规则:
 * 1. reviewId: 必填
 * 2. replyContent: 必填,1-200字
 *
 * 业务规则:
 * - 可以多次修改回复内容
 * - 回复时间由系统自动记录
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Data
@ApiModel(description = "商家回复DTO")
public class ReplyReviewDTO {

    @NotNull(message = "评价ID不能为空")
    @ApiModelProperty(value = "评价ID", required = true, example = "1")
    private Long reviewId;

    @NotBlank(message = "回复内容不能为空")
    @Size(min = 1, max = 200, message = "回复内容长度为1-200字")
    @ApiModelProperty(value = "回复内容", required = true, example = "感谢您的好评!祝您购物愉快!")
    private String replyContent;
}
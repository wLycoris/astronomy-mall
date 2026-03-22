package com.astronomy.mall.module.course.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 提交课程评价 DTO
 * 接口: POST /api/course/{courseId}/review
 *
 * 规则:
 *   rating  必传，1-5 星
 *   content 可选，有内容时不超过 500 字（Service 层截断）
 *   每门课每人只能评价一次（Service 层校验）
 */
@Data
@ApiModel(description = "提交课程评价请求体")
public class CourseReviewSubmitDTO {

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    @ApiModelProperty(value = "评分(1-5星)", required = true, example = "5")
    private Integer rating;

    @ApiModelProperty(value = "评价内容（可选，不超过500字）", example = "课程内容非常丰富！")
    private String content;
}
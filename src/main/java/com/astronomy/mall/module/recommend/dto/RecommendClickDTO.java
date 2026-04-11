package com.astronomy.mall.module.recommend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 推荐点击回写 DTO
 * 前端点击推荐卡片时调用 POST /api/recommend/click
 * 回写 is_clicked=1 + click_time，用于答辩展示点击率
 */
@Data
@ApiModel(description = "推荐点击回写")
public class RecommendClickDTO {

    @NotBlank(message = "推荐类型不能为空")
    @ApiModelProperty(value = "推荐类型: product/course/post", required = true)
    private String recommendType;

    @NotNull(message = "目标ID不能为空")
    @ApiModelProperty(value = "被推荐的目标ID", required = true)
    private Long targetId;
}

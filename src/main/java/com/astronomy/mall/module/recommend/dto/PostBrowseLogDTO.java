package com.astronomy.mall.module.recommend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 帖子浏览埋点 DTO
 * 前端在 ForumDetail 打开时调用 POST /api/recommend/post/browse
 */
@Data
@ApiModel(description = "帖子浏览埋点")
public class PostBrowseLogDTO {

    @NotNull(message = "帖子ID不能为空")
    @ApiModelProperty(value = "帖子ID", required = true)
    private Long postId;

    @ApiModelProperty("浏览时长(秒,可选)")
    private Integer duration;

    @ApiModelProperty("来源页面(可选)")
    private String source;
}

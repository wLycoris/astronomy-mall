package com.astronomy.mall.module.recommend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 推荐帖子 VO
 * 前端推荐帖子卡片展示所需字段
 */
@Data
@ApiModel(description = "推荐帖子")
public class RecommendPostVO {

    @ApiModelProperty("帖子ID")
    private Long id;

    @ApiModelProperty("帖子标题")
    private String title;

    @ApiModelProperty("封面图(images JSON数组的第一张)")
    private String coverImage;

    @ApiModelProperty("帖子标签(JSON数组)")
    private String tags;

    @ApiModelProperty("热度分")
    private Double hotScore;

    @ApiModelProperty("作者昵称")
    private String authorNickname;

    @ApiModelProperty("作者头像")
    private String authorAvatar;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("推荐得分")
    private Double score;

    @ApiModelProperty("推荐算法: content/hot/coldstart")
    private String algorithm;
}

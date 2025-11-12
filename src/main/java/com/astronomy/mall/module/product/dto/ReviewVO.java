package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "评价返回VO")
public class ReviewVO {

    @ApiModelProperty("评价ID")
    private Long id;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("评分(1-5星)")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("评价图片列表")
    private List<String> imageList;

    @ApiModelProperty("是否匿名")
    private Integer isAnonymous;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("商家回复")
    private String reply;

    @ApiModelProperty("回复时间")
    private LocalDateTime replyTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
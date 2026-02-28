package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.*;
import java.util.List;

@Data
@ApiModel(description = "评价VO")
public class ReviewVO {

    @ApiModelProperty("评价ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品图片")
    private String productImage;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("评分(1-5星)")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("评价图片(字符串格式,用于数据库存储)")
    private String images;

    @ApiModelProperty("评价图片列表(前端展示用)")
    private List<String> imageList;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("是否匿名(0-否,1-是)")
    private Integer isAnonymous;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("商家回复内容")
    private String reply;

    @ApiModelProperty("商家回复时间")
    private String replyTime;

    @ApiModelProperty("创建时间")
    private String createTime;

    /**
     * 评价状态: 0-已被管理员删除 1-正常 2-待审核
     * 用途: 我的评价页面根据 status=0 显示"该评价已被管理员删除"
     * 注意: 不再用 deleted 判断，管理员删除只设 status=0，不动 deleted 字段
     */
    @ApiModelProperty("评价状态(0-已被管理员删除 1-正常 2-待审核)")
    private Integer status;
}
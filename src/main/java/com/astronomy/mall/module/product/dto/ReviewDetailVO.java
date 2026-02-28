package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 评价详情VO(高级版)
 *
 * 使用场景:
 * - 高级版评价列表接口的返回数据
 *
 * 特点:
 * 1. 包含完整的用户信息
 * 2. 包含商品信息
 * 3. 包含点赞状态(isLiked)
 * 4. 包含商家回复
 *
 * 与ReviewVO的区别:
 * - ReviewVO: 简化版,用于基础列表
 * - ReviewDetailVO: 完整版,用于高级列表
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Data
@ApiModel(description = "评价详情VO(高级版)")
public class ReviewDetailVO {

    @ApiModelProperty("评价ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("昵称(匿名时显示'匿名用户')")
    private String nickname;

    @ApiModelProperty("头像(匿名时为默认头像)")
    private String avatar;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品图片")
    private String productImage;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty(value = "评分(1-5星)", allowableValues = "range[1,5]")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("评价图片列表(JSON数组)")
    private List<String> images;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty(value = "当前用户是否已点赞", notes = "未登录时为false")
    private Boolean isLiked;

    @ApiModelProperty("商家回复内容")
    private String replyContent;

    @ApiModelProperty("商家回复时间")
    private String replyTime;

    @ApiModelProperty(value = "是否匿名", notes = "true=匿名,false=实名")
    private Boolean isAnonymous;

    @ApiModelProperty(value = "评价状态", notes = "0=已删除,1=正常,2=待审核")
    private Integer status;

    @ApiModelProperty("是否置顶(0-否 1-是)")
    private Integer isTop;

    @ApiModelProperty("创建时间")
    private String createTime;
}
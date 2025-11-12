package com.astronomy.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_review")
@ApiModel(description = "商品评价实体")
public class Review {

    @ApiModelProperty("评价ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("评分(1-5星)")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("评价图片(多张,逗号分隔)")
    private String images;

    @ApiModelProperty("是否匿名(0-否 1-是)")
    private Integer isAnonymous;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("商家回复")
    private String reply;

    @ApiModelProperty("回复时间")
    private LocalDateTime replyTime;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除(0-否 1-是)")
    @TableLogic
    private Integer deleted;
}
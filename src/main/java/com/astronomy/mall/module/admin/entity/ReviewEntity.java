package com.astronomy.mall.module.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品评价实体
 * 对应表: tb_review
 *
 * 📌 评价状态:
 * 0 - 已删除
 * 1 - 正常
 * 2 - 待审核
 */
@Data
@TableName("tb_review")
public class ReviewEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 用户ID */
    private Long userId;

    /** 订单ID */
    private Long orderId;

    /** 评分（1-5星） */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价图片（JSON数组） */
    private String images;

    /** 是否匿名（0-否 1-是） */
    private Integer isAnonymous;

    /** 点赞数 */
    private Integer likeCount;

    /**
     * 评价状态
     * 0 - 已删除
     * 1 - 正常
     * 2 - 待审核
     */
    private Integer status;

    /** 商家回复 */
    private String reply;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 是否置顶（0-否 1-是） */
    private Integer isTop;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    private LocalDateTime topTime;

    /** 逻辑删除（0-否 1-是） */
    @TableLogic
    private Integer deleted;
}
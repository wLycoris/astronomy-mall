package com.astronomy.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品评价实体类
 * 对应数据库表: tb_review
 *
 * 功能说明:
 * 1. 用户对已完成订单的商品进行评价
 * 2. 支持评分(1-5星)、文字内容、图片(最多5张)
 * 3. 支持匿名评价
 * 4. 商家可以回复评价
 * 5. 其他用户可以点赞评价
 *
 * @author Astronomy Mall Team
 * @since 2025-01-XX
 */
@Data
@TableName("tb_review")
public class Review {

    /**
     * 评价ID - 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID - 外键关联 tb_product.id
     */
    private Long productId;

    /**
     * 用户ID - 外键关联 tb_user.id
     */
    private Long userId;

    /**
     * 订单ID - 外键关联 tb_order.id
     * 一个订单只能评价一次
     */
    private Long orderId;

    /**
     * 评分(1-5星)
     * 1星:非常差, 2星:差, 3星:一般, 4星:好, 5星:非常好
     */
    private Integer rating;

    /**
     * 评价内容 - 文字描述
     * 可以为空(只打分不评论)
     */
    private String content;

    /**
     * 评价图片 - JSON数组格式
     * 示例: ["https://xxx.jpg", "https://yyy.jpg"]
     * 最多5张图片
     */
    private String images;

    /**
     * 是否匿名评价
     * 0-否(显示用户昵称), 1-是(显示"匿名用户")
     */
    private Integer isAnonymous;

    /**
     * 点赞数 - 其他用户点赞数量
     * 通过 tb_review_like 表统计
     */
    private Integer likeCount;

    /**
     * 评价状态
     * 0-已删除, 1-正常显示, 2-待审核
     */
    private Integer status;

    /**
     * 商家回复内容
     * 商家可以对用户评价进行一次回复
     */
    private String reply;

    /**
     * 商家回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 举报次数，达到阈值(3次)时 status 自动转为 2(待审核)
     */
    private Integer reportCount;

    /**
     * 创建时间 - 评价发布时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间 - 最后修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     * 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
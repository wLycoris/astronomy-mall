package com.astronomy.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价点赞实体类
 * 对应数据库表: tb_review_like
 *
 * 功能说明:
 * 1. 用户可以对其他用户的评价点赞
 * 2. 每个用户对同一条评价只能点赞一次(唯一索引约束)
 * 3. 点赞后会增加评价的 like_count
 * 4. 取消点赞后会减少评价的 like_count
 *
 * @author Astronomy Mall Team
 * @since 2025-01-XX
 */
@Data
@TableName("tb_review_like")
public class ReviewLike {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评价ID - 外键关联 tb_review.id
     */
    private Long reviewId;

    /**
     * 用户ID - 外键关联 tb_user.id
     * 记录是哪个用户点的赞
     */
    private Long userId;

    /**
     * 点赞时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
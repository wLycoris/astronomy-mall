package com.astronomy.mall.module.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论点赞实体类
 * 对应数据库表: tb_comment_like
 *
 * 📌 与 PostLike 同逻辑:
 *   唯一约束 uk_comment_user(comment_id, user_id)
 *   同一用户对同一评论只能点赞一次
 *   点赞/取消点赞通过 INSERT/DELETE 实现（幂等切换）
 */
@Data
@TableName("tb_comment_like")
public class CommentLike {

    /** 点赞ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论ID（关联tb_post_comment.id） */
    private Long commentId;

    /** 点赞用户ID（关联tb_user.id） */
    private Long userId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

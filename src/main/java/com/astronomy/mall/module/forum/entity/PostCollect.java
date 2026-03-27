package com.astronomy.mall.module.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子收藏实体类
 * 对应数据库表: tb_post_collect
 *
 * 📌 唯一约束: uk_post_user(post_id, user_id)
 *   同一用户对同一帖子只能收藏一次
 *   收藏/取消收藏通过 INSERT/DELETE 实现（幂等切换）
 */
@Data
@TableName("tb_post_collect")
public class PostCollect {

    /** 收藏ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID（关联tb_post.id） */
    private Long postId;

    /** 收藏用户ID（关联tb_user.id） */
    private Long userId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

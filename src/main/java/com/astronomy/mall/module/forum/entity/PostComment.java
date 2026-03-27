package com.astronomy.mall.module.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论实体类
 * 对应数据库表: tb_post_comment
 *
 * 📌 两级评论结构:
 *   parent_id = 0  → 顶级评论
 *   parent_id > 0  → 回复（始终指向顶级评论ID，只做两级，避免无限嵌套）
 *
 * 📌 reply_to_user_id / reply_to_username:
 *   楼中楼回复时记录被回复的用户信息
 *   用于前端显示"回复 @xxx"和发送 COMMENT_REPLIED 通知
 *
 * 📌 status 字段:
 *   0=管理员删除  1=正常
 *   注意: 用户自己删除走 @TableLogic deleted 字段
 */
@Data
@TableName("tb_post_comment")
public class PostComment {

    /** 评论ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID（关联tb_post.id） */
    private Long postId;

    /** 评论用户ID（关联tb_user.id） */
    private Long userId;

    /** 父评论ID: 0=顶级评论, >0=回复(始终指向顶级评论ID) */
    private Long parentId;

    /** 被回复用户ID（@通知依据） */
    private Long replyToUserId;

    /** 被回复用户名（冗余，前端显示"回复 @xxx"用） */
    private String replyToUsername;

    /** 评论内容 */
    private String content;

    /** 评论点赞数(冗余) */
    private Integer likeCount;

    /** 状态: 0管理员删除 1正常 */
    private Integer status;

    /** 逻辑删除(0正常 1删除) — 用户自己删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

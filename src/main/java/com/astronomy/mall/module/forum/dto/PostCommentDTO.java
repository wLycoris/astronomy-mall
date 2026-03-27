package com.astronomy.mall.module.forum.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发布评论/回复 DTO
 *
 * 对应接口: POST /api/post/comment
 *
 * 📌 两级评论:
 *   顶级评论: parentId=0（或不传，默认0），replyToUserId=null
 *   回复: parentId=顶级评论ID，replyToUserId=被回复用户ID
 *   回复的回复: parentId仍为顶级评论ID（不是父回复的ID）
 */
@Data
public class PostCommentDTO {

    /** 帖子ID */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /** 父评论ID: 0=顶级评论, >0=回复(始终指向顶级评论ID) */
    private Long parentId;

    /** 被回复用户ID（回复时必填） */
    private Long replyToUserId;

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    private String content;
}

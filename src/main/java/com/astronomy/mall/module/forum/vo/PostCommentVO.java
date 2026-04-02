package com.astronomy.mall.module.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子评论 VO（两级结构）
 *
 * 对应接口: GET /api/post/{id} 详情页评论区
 *
 * 📌 两级结构:
 *   顶级评论: parentId=0, children 内含回复列表
 *   回复: parentId=顶级评论ID, replyToUsername 显示"回复 @xxx"
 *
 * 📌 字段说明:
 *   nickname/avatar → JOIN tb_user 获取
 *   children → 仅顶级评论有，回复列表（按 create_time 升序）
 */
@Data
public class PostCommentVO {

    /** 评论ID */
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 评论用户ID */
    private Long userId;

    /** 评论用户昵称 */
    private String nickname;

    /** 评论用户头像 */
    private String avatar;

    /** 父评论ID: 0=顶级评论 */
    private Long parentId;

    /** 被回复用户ID */
    private Long replyToUserId;

    /** 被回复用户名（前端显示"回复 @xxx"） */
    private String replyToUsername;

    /** 评论内容 */
    private String content;

    /** 评论点赞数 */
    private Integer likeCount;

    /** 当前用户是否已点赞该评论 */
    private Boolean isLiked;

    /** 回复数量（仅顶级评论有，用于"展开X条回复"显示） */
    private Integer replyCount;

    /** 是否是帖子作者（前端显示"作者"标识） */
    private Boolean isAuthor;

    /** 发布时间 */
    private LocalDateTime createTime;

    /** 子评论列表（仅顶级评论有） */
    private List<PostCommentVO> children;
}

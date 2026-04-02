package com.astronomy.mall.module.forum.service;

import com.astronomy.mall.module.forum.dto.PostCommentDTO;
import com.astronomy.mall.module.forum.vo.PostCommentVO;

import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 *
 * 📌 实现计划:
 *   7.4: addComment / deleteComment / likeComment / getCommentsByPostId
 *
 * 📌 两级评论规则:
 *   parent_id = 0 → 顶级评论
 *   parent_id > 0 → 回复（始终指向顶级评论ID）
 *   回复的回复: parent_id仍为顶级ID，通过 reply_to_user_id 标记被回复人
 */
public interface CommentService {

    /**
     * 发布评论/回复
     *
     * @param userId 评论用户ID
     * @param dto    评论DTO（postId/parentId/replyToUserId/content）
     * @return 评论ID
     */
    Long addComment(Long userId, PostCommentDTO dto);

    /**
     * 删除评论（@TableLogic 逻辑删除，仅评论作者可删）
     *
     * @param userId    当前用户ID
     * @param commentId 评论ID
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 评论点赞（幂等切换，与帖子点赞同逻辑）
     *
     * @param userId    当前用户ID
     * @param commentId 评论ID
     * @return true=已点赞, false=已取消
     */
    boolean likeComment(Long userId, Long commentId);

    /**
     * 获取帖子的评论列表（两级结构）
     *
     * @param postId        帖子ID
     * @param pageNum       页码
     * @param pageSize      每页条数（针对顶级评论分页）
     * @param currentUserId 当前登录用户ID（可为null，用于判断isLiked/isAuthor）
     * @return 两级评论列表
     */
    Map<String, Object> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize, Long currentUserId);
}

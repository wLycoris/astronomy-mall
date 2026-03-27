package com.astronomy.mall.module.forum.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.forum.dto.PostCommentDTO;
import com.astronomy.mall.module.forum.mapper.PostCommentMapper;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.forum.service.CommentService;
import com.astronomy.mall.module.forum.vo.PostCommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 评论服务实现类
 *
 * 📌 核心逻辑:
 *   7.4: 发布评论(两级结构) / 删除评论 / 评论点赞 / 评论列表
 *
 * 📌 两级评论处理:
 *   顶级评论: parentId=0
 *   回复: parentId=顶级评论ID（不管回复谁，parentId都指向顶级）
 *   回复的回复: parentId仍为顶级ID，通过 replyToUserId 标记被回复人
 *
 * 📌 通知触发(7.8):
 *   评论帖子 → POST_COMMENTED 通知帖子作者（防自通知）
 *   回复评论 → COMMENT_REPLIED 通知被回复用户（防自通知）
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private PostCommentMapper postCommentMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long userId, PostCommentDTO dto) {
        // TODO 7.4 实现
        throw new BusinessException("发布评论功能待实现");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        // TODO 7.4 实现
        throw new BusinessException("删除评论功能待实现");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long userId, Long commentId) {
        // TODO 7.4 实现
        throw new BusinessException("评论点赞功能待实现");
    }

    @Override
    public Map<String, Object> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize) {
        // TODO 7.4 实现
        throw new BusinessException("评论列表功能待实现");
    }
}

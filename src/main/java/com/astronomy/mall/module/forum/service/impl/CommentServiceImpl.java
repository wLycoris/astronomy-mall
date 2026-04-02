package com.astronomy.mall.module.forum.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.forum.dto.PostCommentDTO;
import com.astronomy.mall.module.forum.entity.CommentLike;
import com.astronomy.mall.module.forum.entity.Post;
import com.astronomy.mall.module.forum.entity.PostComment;
import com.astronomy.mall.module.forum.mapper.CommentLikeMapper;
import com.astronomy.mall.module.forum.mapper.PostCommentMapper;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.forum.service.CommentService;
import com.astronomy.mall.module.forum.vo.PostCommentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * 📌 核心逻辑:
 *   7.4: 发布评论(两级结构) / 删除评论 / 评论点赞(幂等) / 评论列表
 *
 * 📌 两级评论处理:
 *   顶级评论: parentId=0
 *   回复: parentId=顶级评论ID（不管回复谁，parentId都指向顶级）
 *   回复的回复: parentId仍为顶级ID，通过 replyToUserId 标记被回复人
 *
 * 📌 评论点赞:
 *   与帖子点赞同逻辑 — tb_comment_like 表幂等切换（INSERT/DELETE）
 *   uk_comment_user(comment_id, user_id) 唯一约束防重复
 *
 * 📌 评论列表增强:
 *   isLiked   — 当前用户是否已点赞
 *   isAuthor  — 是否帖子作者（显示"作者"标识）
 *   replyCount — 顶级评论的回复数量（"展开X条回复"用）
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private PostCommentMapper postCommentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    // ==============================
    // 7.4 发布评论/回复
    // ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long userId, PostCommentDTO dto) {
        // 1. 校验帖子存在且已发布（status=2）
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (post.getStatus() != 2) {
            throw new BusinessException("帖子未发布，无法评论");
        }

        // 2. 处理 parentId（默认0=顶级评论）
        Long parentId = dto.getParentId();
        if (parentId == null) {
            parentId = 0L;
        }

        // 3. 如果是回复（parentId>0），校验父评论存在
        String replyToUsername = null;
        if (parentId > 0) {
            PostComment parentComment = postCommentMapper.selectById(parentId);
            if (parentComment == null) {
                throw new BusinessException("被回复的评论不存在");
            }
            // 确保父评论属于同一帖子
            if (!parentComment.getPostId().equals(dto.getPostId())) {
                throw new BusinessException("评论不属于该帖子");
            }
            // 确保parentId指向顶级评论（两级结构规则）
            // 如果回复的是子评论，parentId应该改为其顶级评论ID
            if (parentComment.getParentId() != null && parentComment.getParentId() > 0) {
                parentId = parentComment.getParentId();
            }

            // 查询被回复用户的昵称（冗余存储，前端显示"回复 @xxx"用）
            if (dto.getReplyToUserId() != null) {
                replyToUsername = postCommentMapper.selectNicknameByUserId(dto.getReplyToUserId());
            }
        }

        // 4. 构建评论实体
        PostComment comment = new PostComment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setReplyToUsername(replyToUsername);
        comment.setContent(dto.getContent().trim());
        comment.setLikeCount(0);
        comment.setStatus(1); // 正常状态

        // 5. 插入数据库
        postCommentMapper.insert(comment);
        log.info("评论发布成功: commentId={}, postId={}, userId={}, parentId={}",
                comment.getId(), dto.getPostId(), userId, parentId);

        // 6. 更新帖子评论计数 +1
        Post update = new Post();
        update.setId(dto.getPostId());
        update.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(update);

        // TODO 7.8: 发送评论通知（防自通知）
        // if (!userId.equals(post.getUserId())) { notificationHelper.send(...) }

        return comment.getId();
    }

    // ==============================
    // 7.4 删除评论
    // ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        // 1. 查询评论
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 2. 校验评论作者身份（仅作者本人可删除）
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }

        // 3. 逻辑删除评论（@TableLogic 自动将 deleted 置为1）
        postCommentMapper.deleteById(commentId);
        log.info("评论删除成功(逻辑删除): commentId={}, userId={}", commentId, userId);

        // 4. 更新帖子评论计数 -1
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            // 如果是顶级评论，同时计算其子评论数量一起减
            int decreaseCount = 1;
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                // 顶级评论被删，其下所有子评论也不再显示（但不物理删除子评论）
                Long childCount = postCommentMapper.selectCount(
                        new LambdaQueryWrapper<PostComment>()
                                .eq(PostComment::getParentId, commentId));
                if (childCount != null) {
                    decreaseCount += childCount.intValue();
                }
            }
            Post update = new Post();
            update.setId(comment.getPostId());
            update.setCommentCount(Math.max(0, post.getCommentCount() - decreaseCount));
            postMapper.updateById(update);
        }
    }

    // ==============================
    // 7.4 评论点赞（幂等切换，与帖子点赞同逻辑）
    // ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long userId, Long commentId) {
        // 1. 校验评论存在
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 2. 查询是否已点赞（uk_comment_user 唯一约束）
        Long count = commentLikeMapper.selectCount(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId));

        if (count != null && count > 0) {
            // 3a. 已点赞 → 取消点赞（删除记录 + like_count -1）
            commentLikeMapper.delete(
                    new LambdaQueryWrapper<CommentLike>()
                            .eq(CommentLike::getCommentId, commentId)
                            .eq(CommentLike::getUserId, userId));
            PostComment update = new PostComment();
            update.setId(commentId);
            update.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            postCommentMapper.updateById(update);
            log.info("取消评论点赞: userId={}, commentId={}", userId, commentId);
            return false; // 已取消点赞
        } else {
            // 3b. 未点赞 → 点赞（插入记录 + like_count +1）
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            PostComment update = new PostComment();
            update.setId(commentId);
            update.setLikeCount(comment.getLikeCount() + 1);
            postCommentMapper.updateById(update);
            log.info("评论点赞成功: userId={}, commentId={}", userId, commentId);
            return true; // 已点赞
        }
    }

    // ==============================
    // 7.4 评论列表（两级结构 + isLiked/isAuthor/replyCount）
    // ==============================

    @Override
    public Map<String, Object> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize, Long currentUserId) {
        // 1. 参数校验
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        if (pageSize > 50) pageSize = 50;
        int offset = (pageNum - 1) * pageSize;

        // 2. 获取帖子作者ID（用于标记"作者"标识）
        Post post = postMapper.selectById(postId);
        Long postAuthorId = (post != null) ? post.getUserId() : null;

        // 3. 查询顶级评论（分页），XML已计算 replyCount
        List<Map<String, Object>> topRows = postCommentMapper.selectTopComments(postId, offset, pageSize);
        long total = postCommentMapper.countTopComments(postId);

        // 4. 批量查询当前用户对这些评论的点赞状态
        Set<Long> likedCommentIds = new HashSet<>();
        if (currentUserId != null && !topRows.isEmpty()) {
            // 收集顶级评论ID
            List<Long> topCommentIds = new ArrayList<>();
            for (Map<String, Object> row : topRows) {
                Long cid = toLong(row.get("id"));
                if (cid != null) topCommentIds.add(cid);
            }
            // 查询每个顶级评论的子评论，并收集子评论ID
            Map<Long, List<Map<String, Object>>> childMap = new HashMap<>();
            List<Long> childCommentIds = new ArrayList<>();
            for (Long topId : topCommentIds) {
                List<Map<String, Object>> childRows = postCommentMapper.selectChildComments(topId);
                childMap.put(topId, childRows);
                for (Map<String, Object> childRow : childRows) {
                    Long childId = toLong(childRow.get("id"));
                    if (childId != null) childCommentIds.add(childId);
                }
            }
            // 合并所有评论ID（顶级+子评论）
            List<Long> allCommentIds = new ArrayList<>(topCommentIds);
            allCommentIds.addAll(childCommentIds);

            // 批量查点赞状态
            if (!allCommentIds.isEmpty()) {
                List<CommentLike> likes = commentLikeMapper.selectList(
                        new LambdaQueryWrapper<CommentLike>()
                                .eq(CommentLike::getUserId, currentUserId)
                                .in(CommentLike::getCommentId, allCommentIds));
                likedCommentIds = likes.stream()
                        .map(CommentLike::getCommentId)
                        .collect(Collectors.toSet());
            }

            // 5. 转换为 PostCommentVO 列表（使用已查询的子评论）
            List<PostCommentVO> list = new ArrayList<>();
            for (Map<String, Object> row : topRows) {
                PostCommentVO vo = convertRowToCommentVO(row, postAuthorId, likedCommentIds, currentUserId);
                Long topId = vo.getId();
                List<Map<String, Object>> childRows = childMap.getOrDefault(topId, Collections.emptyList());
                List<PostCommentVO> children = new ArrayList<>();
                for (Map<String, Object> childRow : childRows) {
                    children.add(convertRowToCommentVO(childRow, postAuthorId, likedCommentIds, currentUserId));
                }
                vo.setChildren(children);
                list.add(vo);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            return result;
        }

        // 未登录模式：不查点赞状态
        List<PostCommentVO> list = new ArrayList<>();
        for (Map<String, Object> row : topRows) {
            PostCommentVO vo = convertRowToCommentVO(row, postAuthorId, likedCommentIds, null);
            List<Map<String, Object>> childRows = postCommentMapper.selectChildComments(vo.getId());
            List<PostCommentVO> children = new ArrayList<>();
            for (Map<String, Object> childRow : childRows) {
                children.add(convertRowToCommentVO(childRow, postAuthorId, likedCommentIds, null));
            }
            vo.setChildren(children);
            list.add(vo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 将数据库Map行转换为PostCommentVO
     * 增强：设置 isLiked / isAuthor / replyCount
     */
    private PostCommentVO convertRowToCommentVO(Map<String, Object> row, Long postAuthorId,
                                                 Set<Long> likedCommentIds, Long currentUserId) {
        PostCommentVO vo = new PostCommentVO();
        vo.setId(toLong(row.get("id")));
        vo.setPostId(toLong(row.get("postId")));
        vo.setUserId(toLong(row.get("userId")));
        vo.setNickname((String) row.get("nickname"));
        vo.setAvatar((String) row.get("avatar"));
        vo.setParentId(toLong(row.get("parentId")));
        vo.setReplyToUserId(toLong(row.get("replyToUserId")));
        vo.setReplyToUsername((String) row.get("replyToUsername"));
        vo.setContent((String) row.get("content"));

        // likeCount
        Object likeObj = row.get("likeCount");
        vo.setLikeCount(likeObj instanceof Number ? ((Number) likeObj).intValue() : 0);

        // replyCount（仅顶级评论有此字段，子评论不返回）
        Object replyObj = row.get("replyCount");
        vo.setReplyCount(replyObj instanceof Number ? ((Number) replyObj).intValue() : 0);

        // isLiked — 当前用户是否已点赞
        vo.setIsLiked(vo.getId() != null && likedCommentIds.contains(vo.getId()));

        // isAuthor — 是否帖子作者
        vo.setIsAuthor(postAuthorId != null && postAuthorId.equals(vo.getUserId()));

        // createTime
        Object timeObj = row.get("createTime");
        if (timeObj instanceof LocalDateTime) {
            vo.setCreateTime((LocalDateTime) timeObj);
        }

        return vo;
    }

    /** 安全转Long */
    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return null;
    }
}

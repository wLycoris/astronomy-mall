package com.astronomy.mall.module.forum.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.forum.dto.PostPublishDTO;
import com.astronomy.mall.module.forum.entity.Post;
import com.astronomy.mall.module.forum.entity.PostCollect;
import com.astronomy.mall.module.forum.entity.PostLike;
import com.astronomy.mall.module.forum.mapper.PostCollectMapper;
import com.astronomy.mall.module.forum.mapper.PostLikeMapper;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.forum.mapper.UserFollowMapper;
import com.astronomy.mall.module.forum.service.PostService;
import com.astronomy.mall.module.forum.vo.PostVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 帖子服务实现类
 *
 * 📌 核心逻辑:
 *   7.2: 发帖(auto-approve判断) / 编辑(状态校验) / 删除(@TableLogic)
 *   7.3: 帖子列表(tab分流+分页) / 详情(含互动状态)  — 后续实现
 *   7.4: 点赞/收藏(幂等切换+计数更新)               — 后续实现
 *   7.5: 我的帖子/收藏/用户主页                      — 后续实现
 *
 * 📌 通知集成(7.8):
 *   点赞/收藏/评论时调用 NotificationHelper 发送通知
 *   防自通知: 操作者==作者时跳过
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    /** 帖子自动审核通过开关（true=发布即公开，false=需管理员审核） */
    @Value("${forum.auto-approve:true}")
    private boolean autoApprove;

    // ==============================
    // 7.2 帖子发布/编辑/删除
    // ==============================

    /** 图片最大数量限制 */
    private static final int MAX_IMAGE_COUNT = 9;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishPost(Long userId, PostPublishDTO dto) {
        // 1. 校验图片数量（base64 JSON数组，最多9张）
        validateImages(dto.getImages());

        // 2. 构建帖子实体
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle().trim());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setTags(dto.getTags());
        post.setRecognitionId(dto.getRecognitionId());

        // 3. 根据 auto-approve 配置决定帖子状态
        //    true  → status=2 直接发布（演示模式）
        //    false → status=1 等待管理员审核
        if (autoApprove) {
            post.setStatus(2);
            log.info("自动审核模式: 帖子直接发布, userId={}", userId);
        } else {
            post.setStatus(1);
            log.info("人工审核模式: 帖子进入审核队列, userId={}", userId);
        }

        // 4. 初始化计数字段
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setViewCount(0);
        post.setIsTop(0);
        post.setHotScore(0.0);
        post.setIsHot(0);

        // 5. 插入数据库
        postMapper.insert(post);
        log.info("帖子发布成功: postId={}, userId={}, status={}", post.getId(), userId, post.getStatus());

        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long userId, Long postId, PostPublishDTO dto) {
        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 2. 校验作者身份
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的帖子");
        }

        // 3. 校验帖子状态（仅草稿=0 / 已拒绝=3 可编辑）
        if (post.getStatus() != 0 && post.getStatus() != 3) {
            throw new BusinessException("当前状态不允许编辑，仅草稿或已拒绝的帖子可编辑");
        }

        // 4. 校验图片数量
        validateImages(dto.getImages());

        // 5. 更新字段
        post.setTitle(dto.getTitle().trim());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setTags(dto.getTags());
        post.setRecognitionId(dto.getRecognitionId());

        // 6. 编辑后重新走审核流程
        if (autoApprove) {
            post.setStatus(2);
        } else {
            post.setStatus(1);
        }
        post.setRejectReason(null); // 清除之前的拒绝原因

        postMapper.updateById(post);
        log.info("帖子编辑成功: postId={}, userId={}, newStatus={}", postId, userId, post.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long userId, Long postId) {
        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 2. 校验作者身份
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的帖子");
        }

        // 3. 逻辑删除（@TableLogic 自动将 deleted 置为1）
        postMapper.deleteById(postId);
        log.info("帖子删除成功(逻辑删除): postId={}, userId={}", postId, userId);
    }

    /**
     * 校验图片数量是否超限
     * images 为 base64 JSON数组字符串，最多9张
     */
    private void validateImages(String images) {
        if (!StringUtils.hasText(images)) {
            return;
        }
        try {
            JSONArray arr = JSON.parseArray(images);
            if (arr.size() > MAX_IMAGE_COUNT) {
                throw new BusinessException("最多上传" + MAX_IMAGE_COUNT + "张图片");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("图片数据格式错误，请使用JSON数组");
        }
    }

    // ==============================
    // 7.3 帖子列表+详情
    // ==============================

    @Override
    public Map<String, Object> listPosts(String tab, String tag, Integer pageNum, Integer pageSize, Long currentUserId) {
        // 1. 参数校验
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        if (pageSize > 50) pageSize = 50; // 防止过大请求
        int offset = (pageNum - 1) * pageSize;

        // 2. follow模式需要登录
        if ("follow".equals(tab) && currentUserId == null) {
            tab = "all"; // 未登录时降级为all
        }

        // 3. 查询帖子列表（XML SQL: JOIN tb_user + tab分流排序）
        List<Map<String, Object>> rows = postMapper.selectPostList(tab, tag, currentUserId, offset, pageSize);
        long total = postMapper.countPostList(tab, tag, currentUserId);

        // 4. 转换为PostVO列表（解析images/tags JSON，提取coverImage）
        List<PostVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            PostVO vo = convertRowToPostVO(row);
            // 列表不返回content（节省带宽）
            vo.setContent(null);
            // 列表模式不查互动状态（性能优先）
            vo.setIsLiked(false);
            vo.setIsCollected(false);
            vo.setIsFollowed(false);
            list.add(vo);
        }

        // 5. 组装分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public PostVO getPostDetail(Long postId, Long currentUserId) {
        // 1. 查询帖子详情（XML SQL: JOIN tb_user）
        Map<String, Object> row = postMapper.selectPostDetail(postId);
        if (row == null || row.isEmpty()) {
            throw new BusinessException("帖子不存在");
        }

        // 2. 转换为PostVO
        PostVO vo = convertRowToPostVO(row);

        // 3. 查询当前用户的互动状态（isLiked/isCollected/isFollowed）
        if (currentUserId != null) {
            // 是否已点赞
            Long likeCount = postLikeMapper.selectCount(
                    new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getPostId, postId)
                            .eq(PostLike::getUserId, currentUserId));
            vo.setIsLiked(likeCount != null && likeCount > 0);

            // 是否已收藏
            Long collectCount = postCollectMapper.selectCount(
                    new LambdaQueryWrapper<PostCollect>()
                            .eq(PostCollect::getPostId, postId)
                            .eq(PostCollect::getUserId, currentUserId));
            vo.setIsCollected(collectCount != null && collectCount > 0);

            // 是否已关注作者
            Long postUserId = vo.getUserId();
            if (postUserId != null && !postUserId.equals(currentUserId)) {
                Long followCount = userFollowMapper.selectCount(
                        new LambdaQueryWrapper<com.astronomy.mall.module.forum.entity.UserFollow>()
                                .eq(com.astronomy.mall.module.forum.entity.UserFollow::getFollowerId, currentUserId)
                                .eq(com.astronomy.mall.module.forum.entity.UserFollow::getFollowedId, postUserId));
                vo.setIsFollowed(followCount != null && followCount > 0);
            } else {
                vo.setIsFollowed(false);
            }
        } else {
            vo.setIsLiked(false);
            vo.setIsCollected(false);
            vo.setIsFollowed(false);
        }

        // 4. 浏览量+1（异步增量，不影响主流程）
        try {
            Post update = new Post();
            update.setId(postId);
            update.setViewCount(vo.getViewCount() != null ? vo.getViewCount() + 1 : 1);
            postMapper.updateById(update);
        } catch (Exception e) {
            log.warn("浏览量更新失败, postId={}", postId, e);
        }

        return vo;
    }

    /**
     * 将数据库Map行转换为PostVO
     * 解析 images/tags JSON字符串为List，提取coverImage
     */
    private PostVO convertRowToPostVO(Map<String, Object> row) {
        PostVO vo = new PostVO();
        vo.setId(toLong(row.get("id")));
        vo.setUserId(toLong(row.get("userId")));
        vo.setTitle((String) row.get("title"));
        vo.setContent((String) row.get("content"));
        vo.setStatus(toInt(row.get("status")));
        vo.setRejectReason((String) row.get("rejectReason"));
        vo.setLikeCount(toInt(row.get("likeCount")));
        vo.setCommentCount(toInt(row.get("commentCount")));
        vo.setCollectCount(toInt(row.get("collectCount")));
        vo.setViewCount(toInt(row.get("viewCount")));
        vo.setIsTop(toInt(row.get("isTop")));
        vo.setIsHot(toInt(row.get("isHot")));
        vo.setRecognitionId(toLong(row.get("recognitionId")));
        vo.setAuthorNickname((String) row.get("authorNickname"));
        vo.setAuthorAvatar((String) row.get("authorAvatar"));

        // hotScore
        Object hotScoreObj = row.get("hotScore");
        if (hotScoreObj instanceof Number) {
            vo.setHotScore(((Number) hotScoreObj).doubleValue());
        }

        // authorObservationLevel
        Object levelObj = row.get("authorObservationLevel");
        if (levelObj instanceof Number) {
            vo.setAuthorObservationLevel(((Number) levelObj).intValue());
        }

        // createTime
        Object timeObj = row.get("createTime");
        if (timeObj instanceof java.time.LocalDateTime) {
            vo.setCreateTime((java.time.LocalDateTime) timeObj);
        }

        // 解析 images JSON数组 → List<String>
        String imagesStr = (String) row.get("images");
        if (StringUtils.hasText(imagesStr)) {
            try {
                List<String> imageList = JSON.parseArray(imagesStr, String.class);
                vo.setImages(imageList);
                // 封面图 = 第一张
                if (!imageList.isEmpty()) {
                    vo.setCoverImage(imageList.get(0));
                }
            } catch (Exception e) {
                log.warn("帖子图片JSON解析失败, postId={}", vo.getId());
                vo.setImages(Collections.emptyList());
            }
        } else {
            vo.setImages(Collections.emptyList());
        }

        // 解析 tags JSON数组 → List<String>
        String tagsStr = (String) row.get("tags");
        if (StringUtils.hasText(tagsStr)) {
            try {
                vo.setTags(JSON.parseArray(tagsStr, String.class));
            } catch (Exception e) {
                log.warn("帖子标签JSON解析失败, postId={}", vo.getId());
                vo.setTags(Collections.emptyList());
            }
        } else {
            vo.setTags(Collections.emptyList());
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

    /** 安全转int（默认0） */
    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    // ==============================
    // 7.4 点赞/收藏
    // ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Long userId, Long postId) {
        // 1. 校验帖子存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 2. 查询是否已点赞（uk_post_user 唯一约束）
        Long count = postLikeMapper.selectCount(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserId, userId));

        if (count != null && count > 0) {
            // 3a. 已点赞 → 取消点赞（删除记录 + like_count -1）
            postLikeMapper.delete(
                    new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getPostId, postId)
                            .eq(PostLike::getUserId, userId));
            // 更新帖子点赞计数（防止负数）
            Post update = new Post();
            update.setId(postId);
            update.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(update);
            log.info("取消帖子点赞: userId={}, postId={}", userId, postId);
            return false; // 返回false表示当前未点赞
        } else {
            // 3b. 未点赞 → 点赞（插入记录 + like_count +1）
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            // 更新帖子点赞计数
            Post update = new Post();
            update.setId(postId);
            update.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(update);
            log.info("帖子点赞成功: userId={}, postId={}", userId, postId);
            return true; // 返回true表示当前已点赞
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPost(Long userId, Long postId) {
        // 1. 校验帖子存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 2. 查询是否已收藏（uk_post_user 唯一约束）
        Long count = postCollectMapper.selectCount(
                new LambdaQueryWrapper<PostCollect>()
                        .eq(PostCollect::getPostId, postId)
                        .eq(PostCollect::getUserId, userId));

        if (count != null && count > 0) {
            // 3a. 已收藏 → 取消收藏（删除记录 + collect_count -1）
            postCollectMapper.delete(
                    new LambdaQueryWrapper<PostCollect>()
                            .eq(PostCollect::getPostId, postId)
                            .eq(PostCollect::getUserId, userId));
            Post update = new Post();
            update.setId(postId);
            update.setCollectCount(Math.max(0, post.getCollectCount() - 1));
            postMapper.updateById(update);
            log.info("取消帖子收藏: userId={}, postId={}", userId, postId);
            return false; // 返回false表示当前未收藏
        } else {
            // 3b. 未收藏 → 收藏（插入记录 + collect_count +1）
            PostCollect collect = new PostCollect();
            collect.setPostId(postId);
            collect.setUserId(userId);
            postCollectMapper.insert(collect);
            Post update = new Post();
            update.setId(postId);
            update.setCollectCount(post.getCollectCount() + 1);
            postMapper.updateById(update);
            log.info("帖子收藏成功: userId={}, postId={}", userId, postId);
            return true; // 返回true表示当前已收藏
        }
    }

    // ==============================
    // 7.5 我的帖子/收藏/用户主页
    // ==============================

    @Override
    public Map<String, Object> getMyPosts(Long userId, Integer pageNum, Integer pageSize) {
        // TODO 7.5 实现
        throw new BusinessException("我的帖子功能待实现");
    }

    @Override
    public Map<String, Object> getMyCollects(Long userId, Integer pageNum, Integer pageSize) {
        // TODO 7.5 实现
        throw new BusinessException("我的收藏功能待实现");
    }

    @Override
    public Map<String, Object> getUserProfile(Long targetUserId, Long currentUserId) {
        // TODO 7.5 实现
        throw new BusinessException("用户主页功能待实现");
    }
}

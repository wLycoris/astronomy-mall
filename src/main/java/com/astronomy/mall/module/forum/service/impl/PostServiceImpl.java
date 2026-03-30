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

import java.util.Map;

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
        // TODO 7.3 实现
        throw new BusinessException("帖子列表功能待实现");
    }

    @Override
    public PostVO getPostDetail(Long postId, Long currentUserId) {
        // TODO 7.3 实现
        throw new BusinessException("帖子详情功能待实现");
    }

    // ==============================
    // 7.4 点赞/收藏
    // ==============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Long userId, Long postId) {
        // TODO 7.4 实现
        throw new BusinessException("帖子点赞功能待实现");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectPost(Long userId, Long postId) {
        // TODO 7.4 实现
        throw new BusinessException("帖子收藏功能待实现");
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

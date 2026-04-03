package com.astronomy.mall.module.forum.service;

import com.astronomy.mall.module.forum.dto.PostPublishDTO;
import com.astronomy.mall.module.forum.vo.PostVO;

import java.util.Map;

/**
 * 帖子服务接口
 *
 * 📌 实现计划:
 *   7.2: publishPost / updatePost / deletePost
 *   7.3: listPosts / getPostDetail
 *   7.4: likePost / collectPost
 *   7.5: getMyPosts / getMyCollects / getUserProfile
 */
public interface PostService {

    // ==============================
    // 7.2 帖子发布/编辑/删除
    // ==============================

    /**
     * 发布帖子
     * auto-approve=true → status=2直接发布; false → status=1等审核
     *
     * @param userId 发帖用户ID
     * @param dto    发帖DTO（title/content/images/tags/recognitionId）
     * @return 帖子ID
     */
    Long publishPost(Long userId, PostPublishDTO dto);

    /**
     * 编辑帖子（仅草稿/已拒绝状态可编辑）
     *
     * @param userId 当前用户ID（校验是否为帖子作者）
     * @param postId 帖子ID
     * @param dto    修改内容
     */
    void updatePost(Long userId, Long postId, PostPublishDTO dto);

    /**
     * 删除帖子（@TableLogic 逻辑删除）
     *
     * @param userId 当前用户ID（校验是否为帖子作者）
     * @param postId 帖子ID
     */
    void deletePost(Long userId, Long postId);

    // ==============================
    // 7.3 帖子列表+详情（后续实现）
    // ==============================

    /**
     * 帖子列表（瀑布流分页）
     *
     * @param tab           标签: all/follow/hot
     * @param tag           分类标签筛选（可为null）
     * @param pageNum       页码
     * @param pageSize      每页条数
     * @param currentUserId 当前登录用户ID（未登录为null）
     * @return 分页结果 {list, total, pageNum, pageSize}
     */
    Map<String, Object> listPosts(String tab, String tag, Integer pageNum, Integer pageSize, Long currentUserId);

    /**
     * 帖子详情（含作者信息/isLiked/isCollected）
     *
     * @param postId        帖子ID
     * @param currentUserId 当前登录用户ID（未登录为null）
     * @return 帖子详情VO
     */
    PostVO getPostDetail(Long postId, Long currentUserId);

    // ==============================
    // 7.4 点赞/收藏（后续实现）
    // ==============================

    /**
     * 帖子点赞/取消点赞（幂等切换）
     *
     * @param userId 当前用户ID
     * @param postId 帖子ID
     * @return true=点赞成功, false=取消点赞
     */
    boolean likePost(Long userId, Long postId);

    /**
     * 帖子收藏/取消收藏（幂等切换）
     *
     * @param userId 当前用户ID
     * @param postId 帖子ID
     * @return true=收藏成功, false=取消收藏
     */
    boolean collectPost(Long userId, Long postId);

    // ==============================
    // 7.5 我的帖子/收藏/用户主页（后续实现）
    // ==============================

    /**
     * 我发布的帖子（分页）
     */
    Map<String, Object> getMyPosts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 我收藏的帖子（分页）
     */
    Map<String, Object> getMyCollects(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 我点赞的帖子（分页）
     */
    Map<String, Object> getMyLikes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 用户主页信息（帖子数/收藏数/关注数/粉丝数/isFollowed）
     *
     * @param targetUserId  目标用户ID
     * @param currentUserId 当前登录用户ID（判断isFollowed用，未登录为null）
     * @return 用户主页VO
     */
    Map<String, Object> getUserProfile(Long targetUserId, Long currentUserId);
}

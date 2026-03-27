package com.astronomy.mall.module.forum.service;

import java.util.Map;

/**
 * 关注服务接口
 *
 * 📌 实现计划:
 *   7.5: follow / getFollowList / getFansList
 *
 * 📌 关注方向:
 *   followerId 关注了 followedId
 *   "我关注的人" → follower_id = currentUserId
 *   "关注我的人" → followed_id = currentUserId
 */
public interface FollowService {

    /**
     * 关注/取消关注（幂等切换）
     *
     * @param currentUserId 当前用户ID（关注者）
     * @param targetUserId  目标用户ID（被关注者）
     * @return true=关注成功, false=取消关注
     */
    boolean follow(Long currentUserId, Long targetUserId);

    /**
     * 我关注的人列表（分页）
     *
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果 {list, total, pageNum, pageSize}
     */
    Map<String, Object> getFollowList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 关注我的人（粉丝）列表（分页）
     *
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果 {list, total, pageNum, pageSize}
     */
    Map<String, Object> getFansList(Long userId, Integer pageNum, Integer pageSize);
}

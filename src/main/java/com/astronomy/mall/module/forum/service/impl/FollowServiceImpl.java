package com.astronomy.mall.module.forum.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.forum.entity.UserFollow;
import com.astronomy.mall.module.forum.mapper.UserFollowMapper;
import com.astronomy.mall.module.forum.service.FollowService;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 关注服务实现类
 *
 * 📌 核心逻辑:
 *   7.5: 关注/取消关注(幂等切换) / 关注列表 / 粉丝列表
 *
 * 📌 幂等切换:
 *   已关注 → DELETE → 取消关注
 *   未关注 → INSERT → 关注成功
 *
 * 📌 通知触发:
 *   关注成功 → USER_FOLLOWED 通知被关注用户
 */
@Slf4j
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long currentUserId, Long targetUserId) {
        // 1. 不能关注自己
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException("不能关注自己");
        }

        // 2. 校验目标用户存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 查询是否已关注（uk_follow 唯一约束）
        Long count = userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, currentUserId)
                        .eq(UserFollow::getFollowedId, targetUserId));

        if (count != null && count > 0) {
            // 4a. 已关注 → 取消关注（删除记录）
            userFollowMapper.delete(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowerId, currentUserId)
                            .eq(UserFollow::getFollowedId, targetUserId));
            log.info("取消关注: {} → {}", currentUserId, targetUserId);
            return false; // 返回false表示当前未关注
        } else {
            // 4b. 未关注 → 关注（插入记录）
            UserFollow follow = new UserFollow();
            follow.setFollowerId(currentUserId);
            follow.setFollowedId(targetUserId);
            userFollowMapper.insert(follow);
            log.info("关注成功: {} → {}", currentUserId, targetUserId);

            // 5. 发送关注通知（异步，不影响主流程）
            notificationHelper.sendUserFollowedNotification(targetUserId, currentUserId);

            return true; // 返回true表示当前已关注
        }
    }

    @Override
    public Map<String, Object> getFollowList(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 参数校验
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        if (pageSize > 50) pageSize = 50;

        // 2. 分页查询关注关系（follower_id = 当前用户 → 查我关注的人）
        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        Page<UserFollow> followPage = userFollowMapper.selectPage(page,
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
                        .orderByDesc(UserFollow::getCreateTime));

        // 3. 批量查询被关注用户信息 + 判断是否互相关注
        List<Map<String, Object>> list = new ArrayList<>();
        if (!followPage.getRecords().isEmpty()) {
            List<Long> followedIds = followPage.getRecords().stream()
                    .map(UserFollow::getFollowedId)
                    .collect(Collectors.toList());
            // 批量查询用户
            List<User> users = userMapper.selectBatchIds(followedIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

            // 查询这些人是否也关注了我（互关判断）
            List<UserFollow> reverseFollows = userFollowMapper.selectList(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowedId, userId)
                            .in(UserFollow::getFollowerId, followedIds));
            Set<Long> mutualFollowerIds = reverseFollows.stream()
                    .map(UserFollow::getFollowerId)
                    .collect(Collectors.toSet());

            for (UserFollow f : followPage.getRecords()) {
                User user = userMap.get(f.getFollowedId());
                if (user != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", user.getId());
                    item.put("nickname", user.getNickname());
                    item.put("avatar", user.getAvatar());
                    item.put("observationLevel", user.getObservationLevel());
                    item.put("city", user.getCity());
                    item.put("followTime", f.getCreateTime());
                    item.put("isFollowed", mutualFollowerIds.contains(user.getId())); // 对方是否也关注了我（互关）
                    list.add(item);
                }
            }
        }

        // 4. 组装分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", followPage.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Map<String, Object> getFansList(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 参数校验
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        if (pageSize > 50) pageSize = 50;

        // 2. 分页查询粉丝关系（followed_id = 当前用户 → 查关注我的人）
        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        Page<UserFollow> fansPage = userFollowMapper.selectPage(page,
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowedId, userId)
                        .orderByDesc(UserFollow::getCreateTime));

        // 3. 批量查询粉丝用户信息 + 判断是否互相关注
        List<Map<String, Object>> list = new ArrayList<>();
        if (!fansPage.getRecords().isEmpty()) {
            List<Long> followerIds = fansPage.getRecords().stream()
                    .map(UserFollow::getFollowerId)
                    .collect(Collectors.toList());
            // 批量查询用户
            List<User> users = userMapper.selectBatchIds(followerIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

            // 查询我是否也关注了这些粉丝（互关判断）
            List<UserFollow> myFollows = userFollowMapper.selectList(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowerId, userId)
                            .in(UserFollow::getFollowedId, followerIds));
            Set<Long> myFollowedIds = myFollows.stream()
                    .map(UserFollow::getFollowedId)
                    .collect(Collectors.toSet());

            for (UserFollow f : fansPage.getRecords()) {
                User user = userMap.get(f.getFollowerId());
                if (user != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", user.getId());
                    item.put("nickname", user.getNickname());
                    item.put("avatar", user.getAvatar());
                    item.put("observationLevel", user.getObservationLevel());
                    item.put("city", user.getCity());
                    item.put("followTime", f.getCreateTime());
                    item.put("isFollowed", myFollowedIds.contains(user.getId())); // 我是否也关注了ta
                    list.add(item);
                }
            }
        }

        // 4. 组装分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", fansPage.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }
}

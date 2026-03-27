package com.astronomy.mall.module.forum.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.forum.mapper.UserFollowMapper;
import com.astronomy.mall.module.forum.service.FollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
 * 📌 通知触发(7.8):
 *   关注成功 → USER_FOLLOWED 通知被关注用户
 */
@Slf4j
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long currentUserId, Long targetUserId) {
        // TODO 7.5 实现
        throw new BusinessException("关注功能待实现");
    }

    @Override
    public Map<String, Object> getFollowList(Long userId, Integer pageNum, Integer pageSize) {
        // TODO 7.5 实现
        throw new BusinessException("关注列表功能待实现");
    }

    @Override
    public Map<String, Object> getFansList(Long userId, Integer pageNum, Integer pageSize) {
        // TODO 7.5 实现
        throw new BusinessException("粉丝列表功能待实现");
    }
}

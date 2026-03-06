package com.astronomy.mall.module.user.service;

import com.astronomy.mall.module.user.vo.UserOverviewVO;

/**
 * 个人中心概览 Service
 *
 * 📌 职责：聚合查询用户概览页所有数据
 *         一个接口返回：用户信息 + 订单状态统计 + 钱包余额 + 最近流水
 */
public interface UserOverviewService {

    /**
     * 获取个人中心概览数据
     *
     * @param userId 当前登录用户ID
     * @return 聚合概览VO
     */
    UserOverviewVO getOverview(Long userId);
}
package com.astronomy.mall.module.recommend.service;

import java.util.Map;

/**
 * Item-based 协同过滤推荐 Service
 *
 * 算法说明：
 * sim(i,j) = |N(i) ∩ N(j)| / sqrt(|N(i)| × |N(j)|)
 * 其中 N(i) = 与商品 i 有行为的用户集合
 * 行为权重: 浏览(1) + 收藏(2) + 购买(3)
 *
 * demo 模式（recommend.cf-precompute=false）: 每次请求现算 Top N 相似商品
 * 生产模式（recommend.cf-precompute=true）: RecommendScheduler 每6小时预计算矩阵存 Redis
 */
public interface CfRecommendService {

    /**
     * 计算与指定商品最相似的 Top N 商品
     * @param productId 目标商品ID
     * @param topN 返回数量
     * @return Map<商品ID, 相似度得分>，按得分降序
     */
    Map<Long, Double> computeCfSimilar(Long productId, int topN);

    /**
     * 为用户生成 CF 推荐候选商品
     * 聚合用户 browse + favorite + order 三路行为，找到用户偏好商品的相似商品
     * @param userId 用户ID
     * @param topN 返回数量
     * @return Map<商品ID, CF得分>
     */
    Map<Long, Double> getCfRecommendForUser(Long userId, int topN);
}

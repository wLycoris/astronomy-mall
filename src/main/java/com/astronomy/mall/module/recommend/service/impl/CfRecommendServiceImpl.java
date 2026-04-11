package com.astronomy.mall.module.recommend.service.impl;

import com.astronomy.mall.module.favorite.entity.ProductFavorite;
import com.astronomy.mall.module.favorite.mapper.ProductFavoriteMapper;
import com.astronomy.mall.module.order.entity.OrderItem;
import com.astronomy.mall.module.order.mapper.OrderItemMapper;
import com.astronomy.mall.module.recommend.mapper.BrowseLogMapper;
import com.astronomy.mall.module.recommend.service.CfRecommendService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Item-based 协同过滤推荐实现
 *
 * 📌 算法（论文第 4 章对应）:
 * sim(i,j) = |N(i) ∩ N(j)| / sqrt(|N(i)| × |N(j)|)
 * 其中 N(i) = 与商品 i 有行为的用户集合（去重后）
 *
 * 📌 行为权重:
 * - 浏览(browse): 权重 1
 * - 收藏(favorite): 权重 2
 * - 购买(order): 权重 3
 *
 * 📌 demo 模式 vs 生产模式:
 * - demo (cf-precompute=false): 每次请求现算，用户少无性能压力
 * - 生产 (cf-precompute=true): RecommendScheduler 每 6 小时预计算矩阵存 Redis
 *   答辩话术："demo 模式为方便演示采用现算，生产环境启用预计算以支撑大用户量"
 */
@Slf4j
@Service
public class CfRecommendServiceImpl implements CfRecommendService {

    @Resource
    private BrowseLogMapper browseLogMapper;
    @Resource
    private ProductFavoriteMapper productFavoriteMapper;
    @Resource
    private OrderItemMapper orderItemMapper;

    @Value("${recommend.cf-precompute:false}")
    private boolean cfPrecompute;

    /**
     * 计算与指定商品最相似的 Top N 商品
     *
     * 步骤:
     * 1. 查找所有与 productId 有行为的用户
     * 2. 查找这些用户行为过的其他商品
     * 3. 按余弦相似度公式计算得分
     * 4. 排序返回 Top N
     */
    @Override
    public Map<Long, Double> computeCfSimilar(Long productId, int topN) {
        // 1. 查找与该商品有浏览行为的用户
        List<Long> usersOfTarget = browseLogMapper.selectUserIdsByProductId(productId);
        if (usersOfTarget.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> targetUserSet = new HashSet<>(usersOfTarget);

        // 2. 收集这些用户浏览过的所有其他商品 → Map<otherProductId, Set<userId>>
        Map<Long, Set<Long>> otherProductUsers = new HashMap<>();
        for (Long userId : targetUserSet) {
            List<Long> browsedProducts = browseLogMapper.selectRecentProductIds(userId, 100);
            for (Long pid : browsedProducts) {
                if (!pid.equals(productId)) {
                    otherProductUsers.computeIfAbsent(pid, k -> new HashSet<>()).add(userId);
                }
            }
        }

        // 3. 按余弦相似度公式计算
        // sim(i,j) = |N(i) ∩ N(j)| / sqrt(|N(i)| × |N(j)|)
        Map<Long, Double> similarityMap = new HashMap<>();
        int targetSize = targetUserSet.size();

        for (Map.Entry<Long, Set<Long>> entry : otherProductUsers.entrySet()) {
            Long otherPid = entry.getKey();
            Set<Long> otherUsers = entry.getValue();

            // 计算交集: 与两个商品都有行为的用户数
            long intersection = otherUsers.stream()
                    .filter(targetUserSet::contains)
                    .count();

            if (intersection > 0) {
                double similarity = intersection / Math.sqrt((double) targetSize * otherUsers.size());
                similarityMap.put(otherPid, similarity);
            }
        }

        // 4. 排序返回 Top N
        return similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * 为用户生成 CF 推荐候选商品
     *
     * 步骤:
     * 1. 聚合用户三路行为: browse(权重1) + favorite(权重2) + order(权重3)
     * 2. 对每个行为商品计算 CF 相似商品
     * 3. 按加权得分汇总排序
     */
    @Override
    public Map<Long, Double> getCfRecommendForUser(Long userId, int topN) {
        // 1. 构建用户行为矩阵 Map<productId, behaviorScore>
        Map<Long, Double> userBehavior = buildUserBehavior(userId);
        if (userBehavior.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. 对每个有行为的商品，获取 CF 相似商品，加权汇总
        Map<Long, Double> candidateScores = new HashMap<>();
        for (Map.Entry<Long, Double> entry : userBehavior.entrySet()) {
            Long productId = entry.getKey();
            double behaviorWeight = entry.getValue();

            Map<Long, Double> similar = computeCfSimilar(productId, 10);
            for (Map.Entry<Long, Double> simEntry : similar.entrySet()) {
                Long simPid = simEntry.getKey();
                double simScore = simEntry.getValue();

                // 排除用户已有行为的商品
                if (!userBehavior.containsKey(simPid)) {
                    candidateScores.merge(simPid, simScore * behaviorWeight, Double::sum);
                }
            }
        }

        // 3. 排序返回 Top N
        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * 构建用户行为矩阵
     * 聚合 browse(1) + favorite(2) + order(3) 三路行为
     * 取每个商品的最高权重（不累加，避免刷浏览抬分）
     */
    private Map<Long, Double> buildUserBehavior(Long userId) {
        Map<Long, Double> behavior = new HashMap<>();

        // 浏览行为 (权重 1)
        List<Map<String, Object>> browseCounts = browseLogMapper.selectUserBrowseCounts(userId);
        for (Map<String, Object> row : browseCounts) {
            Long productId = ((Number) row.get("productId")).longValue();
            behavior.put(productId, 1.0);
        }

        // 收藏行为 (权重 2，覆盖浏览权重)
        LambdaQueryWrapper<ProductFavorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(ProductFavorite::getUserId, userId);
        List<ProductFavorite> favorites = productFavoriteMapper.selectList(favWrapper);
        for (ProductFavorite fav : favorites) {
            behavior.merge(fav.getProductId(), 2.0, Math::max);
        }

        // 购买行为 (权重 3，最高权重)
        LambdaQueryWrapper<OrderItem> orderWrapper = new LambdaQueryWrapper<>();
        // 通过子查询匹配该用户的订单
        orderWrapper.inSql(OrderItem::getOrderId,
                "SELECT id FROM tb_order WHERE user_id = " + userId + " AND status >= 2");
        List<OrderItem> orderItems = orderItemMapper.selectList(orderWrapper);
        for (OrderItem item : orderItems) {
            behavior.merge(item.getProductId(), 3.0, Math::max);
        }

        return behavior;
    }
}

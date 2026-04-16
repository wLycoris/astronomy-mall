package com.astronomy.mall.module.favorite.task;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.module.favorite.entity.ProductFavorite;
import com.astronomy.mall.module.favorite.mapper.ProductFavoriteMapper;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品降价检测定时任务
 *
 * 📌 路径: com.astronomy.mall.module.favorite.task.PriceDropScheduler
 * 📌 执行时机: 每天凌晨2点（低峰期）
 *
 * 逻辑说明:
 *   1. 查询所有收藏记录
 *   2. 对比每条收藏记录中的 product_price（收藏时价格）与商品当前价格
 *   3. 如果当前价格 < 收藏时价格，则发送降价通知
 *   4. 🆕 8.4 新增: 如果降价 + 用户 interest_tags 命中商品 tags，额外发送推荐通知
 *
 * ⚠️ 注意事项:
 *   1. 同一用户同一商品可能重复通知（下次降价再通知），属正常逻辑
 *   2. 已下架/删除的商品跳过（不通知）
 *   3. 通知使用 @Async 异步发送，不阻塞定时任务
 *
 * ⚠️ 启用条件:
 *   确保启动类或配置类有 @EnableScheduling 注解
 *   (Spring Boot 默认不开启定时任务，需要手动启用)
 */
@Slf4j
@Component
public class PriceDropScheduler {

    @Autowired
    private ProductFavoriteMapper productFavoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private NotificationHelper notificationHelper;

    // 🆕 8.4: 注入 UserMapper，用于获取用户 interest_tags 做推荐判断
    @Autowired
    private UserMapper userMapper;

    /**
     * 每天凌晨 2:00 检测收藏商品是否降价
     *
     * cron 表达式: 秒 分 时 日 月 周
     * "0 0 2 * * ?"  → 每天 02:00:00 执行
     * 测试用例: "0 * * * * ?" → 每分钟执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkPriceDrop() {
        log.info("[降价检测] 开始执行降价检测定时任务...");
        int notifyCount = 0;
        int recommendCount = 0;  // 🆕 8.4: 推荐通知计数

        try {
            // 1. 查询所有收藏记录
            List<ProductFavorite> allFavorites = productFavoriteMapper.selectList(
                    new LambdaQueryWrapper<ProductFavorite>()
                            // 只处理有收藏价格记录的条目
                            .isNotNull(ProductFavorite::getProductPrice)
            );

            log.info("[降价检测] 共找到 {} 条收藏记录", allFavorites.size());

            for (ProductFavorite favorite : allFavorites) {
                try {
                    // 2. 查询商品当前信息（只查上架且未删除的商品）
                    Product product = productMapper.selectOne(
                            new LambdaQueryWrapper<Product>()
                                    .eq(Product::getId, favorite.getProductId())
                                    .eq(Product::getStatus, 1)  // 上架中
                                    .eq(Product::getDeleted, 0) // 未删除
                    );

                    // 商品已下架/删除，跳过
                    if (product == null) {
                        continue;
                    }

                    BigDecimal currentPrice = product.getPrice();
                    BigDecimal favoritePrice = favorite.getProductPrice();

                    // 3. 判断是否降价（当前价格严格小于收藏时价格）
                    if (currentPrice != null && currentPrice.compareTo(favoritePrice) < 0) {
                        // 4. 发送降价通知（异步，不阻塞循环）
                        notificationHelper.sendProductPriceDownNotification(
                                favorite.getUserId(),
                                product.getProductName(),
                                currentPrice.toPlainString(),
                                product.getId()
                        );
                        notifyCount++;

                        // ── 🆕 8.4: 降价 + interest_tags 命中 → 额外发送推荐通知 ──
                        // 不是每次降价都推荐，只有用户兴趣标签与商品标签有交集时才推
                        // 目的：把"为你推荐"通知和普通降价通知区分开，推荐通知出现在「推荐」Tab
                        try {
                            if (hasInterestTagOverlap(favorite.getUserId(), product.getTags())) {
                                notificationHelper.sendProductRecommendNotification(
                                        favorite.getUserId(),
                                        product.getProductName(),
                                        currentPrice.toPlainString(),
                                        product.getId()
                                );
                                recommendCount++;
                            }
                        } catch (Exception e) {
                            // 推荐通知失败不影响降价通知主流程
                            log.warn("[降价检测] 推荐通知发送失败, userId={}, productId={}",
                                    favorite.getUserId(), product.getId(), e);
                        }
                        // ── 8.4 推荐通知结束 ──
                    }

                } catch (Exception e) {
                    // 单条记录处理失败不影响整体任务
                    log.error("[降价检测] 处理收藏记录失败, favoriteId={}, userId={}, productId={}",
                            favorite.getId(), favorite.getUserId(), favorite.getProductId(), e);
                }
            }

        } catch (Exception e) {
            log.error("[降价检测] 定时任务执行失败", e);
        }

        log.info("[降价检测] 定时任务完成, 共发送降价通知 {} 条, 推荐通知 {} 条",
                notifyCount, recommendCount);
    }

    // ============================================================
    // 🆕 8.4: interest_tags 交集判断
    // ============================================================

    /**
     * 判断用户 interest_tags 与商品 tags 是否有交集
     *
     * 📌 interest_tags 格式: JSON 数组 ["深空摄影","行星观测"] 或 CSV "深空摄影,行星观测"
     * 📌 product.tags   格式: JSON 数组 ["深空摄影","天文相机"] 或 CSV "深空摄影,天文相机"
     * 📌 兼容两种格式（与 RecommendServiceImpl.parseTags 一致）
     *
     * @param userId      用户 ID
     * @param productTags 商品 tags 字段（JSON 或 CSV 字符串）
     * @return 有交集返回 true
     */
    private boolean hasInterestTagOverlap(Long userId, String productTags) {
        if (userId == null || productTags == null || productTags.isEmpty()) {
            return false;
        }

        // 1. 获取用户 interest_tags
        User user = userMapper.selectById(userId);
        if (user == null || user.getInterestTags() == null || user.getInterestTags().isEmpty()) {
            return false;
        }

        // 2. 解析双方标签
        Set<String> userTags = parseTags(user.getInterestTags());
        Set<String> pTags = parseTags(productTags);

        if (userTags.isEmpty() || pTags.isEmpty()) {
            return false;
        }

        // 3. 交集判断（任意一个命中即可）
        for (String tag : userTags) {
            if (pTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安全解析标签字符串（兼容 JSON 数组 + CSV 两种格式）
     * 与 RecommendServiceImpl.parseTags 逻辑一致，避免跨模块耦合
     */
    private Set<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return Collections.emptySet();
        }
        tagsStr = tagsStr.trim();

        // 尝试 JSON 数组
        if (tagsStr.startsWith("[")) {
            try {
                List<String> list = JSON.parseArray(tagsStr, String.class);
                if (list != null) {
                    return list.stream()
                            .filter(s -> s != null && !s.trim().isEmpty())
                            .map(String::trim)
                            .collect(Collectors.toSet());
                }
            } catch (Exception ignored) {
                // JSON 解析失败，降级到 CSV
            }
        }

        // 降级到 CSV
        return Arrays.stream(tagsStr.split("[,，]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}

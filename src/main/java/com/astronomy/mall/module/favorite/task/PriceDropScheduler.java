package com.astronomy.mall.module.favorite.task;

import com.astronomy.mall.module.favorite.entity.ProductFavorite;
import com.astronomy.mall.module.favorite.mapper.ProductFavoriteMapper;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

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
                                currentPrice.toPlainString(),  // 🔧 修复: String price，传当前价格
                                product.getId()
                        );
                        notifyCount++;
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

        log.info("[降价检测] 定时任务完成, 共发送降价通知 {} 条", notifyCount);
    }
}
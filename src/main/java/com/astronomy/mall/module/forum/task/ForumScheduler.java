package com.astronomy.mall.module.forum.task;

import com.astronomy.mall.module.forum.entity.Post;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 论坛定时任务
 *
 * 📌 热度计算（每小时执行一次）:
 *   1. 查 status=2(已发布) + 近7天帖子
 *   2. score = (likes×1 + comments×2 + collects×3) / Math.pow(天数+2, 1.5)
 *   3. 批量UPDATE hot_score, is_hot
 *   4. is_hot 从 0→1 的帖子 → sendPostTrendingNotification（每帖一生只发一次）
 *
 * 📌 阈值说明:
 *   HOT_SCORE_THRESHOLD = 3.0 (demo级阈值)
 *   公式分母在 day0 ≈ 2.83, day3 ≈ 11.18, day7 ≈ 27.0
 *   day0 想达到 3.0 → 加权交互约 8.5（如 5赞+1评论+1收藏 = 5+2+3 = 10/2.83 ≈ 3.5）
 *   阈值刻意设小以便演示;生产可调到 50+ 配合冷启动期排除
 *
 * 📌 一生只发一次:
 *   通过判断 is_hot 字段从 0 → 1 的转换实现 - 一旦置 1 后续即使分数下降也不再重发。
 *   注意:不会主动把热门帖子降回非热门(即不存在 1 → 0)。
 *
 * 📌 依赖:
 *   @EnableScheduling 已在 AstronomyMallApplication 上启用
 */
@Slf4j
@Component
public class ForumScheduler {

    @Autowired
    private PostMapper postMapper;

    /** 7.8: 帖子升为热门时发送通知 */
    @Autowired
    private NotificationHelper notificationHelper;

    /** 热度阈值（demo 模式偏低，便于触发通知演示） */
    private static final double HOT_SCORE_THRESHOLD = 3.0;

    /** 热度计算窗口（天） */
    private static final int HOT_SCORE_WINDOW_DAYS = 7;

    /**
     * 每小时计算帖子热度分
     * cron: 秒 分 时 日 月 周
     * "0 0 * * * ?" = 每小时整点执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calcHotScores() {
        long startTime = System.currentTimeMillis();
        try {
            // 1. 查 status=2 (已发布) + 近 7 天帖子
            LocalDateTime since = LocalDateTime.now().minusDays(HOT_SCORE_WINDOW_DAYS);
            List<Post> posts = postMapper.selectList(
                    new LambdaQueryWrapper<Post>()
                            .eq(Post::getStatus, 2)
                            .ge(Post::getCreateTime, since));

            if (posts == null || posts.isEmpty()) {
                log.debug("论坛热度计算: 近{}天无已发布帖子，跳过", HOT_SCORE_WINDOW_DAYS);
                return;
            }

            int updatedCount = 0;
            int trendingCount = 0;
            LocalDateTime now = LocalDateTime.now();

            // 2. 逐帖计算 + 单条 update（小型论坛，量级小，不引入批量SQL）
            for (Post post : posts) {
                int likes    = post.getLikeCount()    == null ? 0 : post.getLikeCount();
                int comments = post.getCommentCount() == null ? 0 : post.getCommentCount();
                int collects = post.getCollectCount() == null ? 0 : post.getCollectCount();

                // 已发布天数（小数）
                double daysSince = Duration.between(post.getCreateTime(), now).toMinutes() / (60.0 * 24.0);
                if (daysSince < 0) daysSince = 0;

                double score = (likes * 1.0 + comments * 2.0 + collects * 3.0)
                        / Math.pow(daysSince + 2, 1.5);

                int newIsHot = score >= HOT_SCORE_THRESHOLD ? 1 : 0;
                int oldIsHot = post.getIsHot() == null ? 0 : post.getIsHot();

                // 一生只发一次:已经是热门则保持 1，不会降回 0
                int finalIsHot = (oldIsHot == 1) ? 1 : newIsHot;

                Post update = new Post();
                update.setId(post.getId());
                update.setHotScore(score);
                update.setIsHot(finalIsHot);
                postMapper.updateById(update);
                updatedCount++;

                // 3. is_hot 从 0 → 1 → 发送热门通知（防止重复发送由 oldIsHot 判定保证）
                if (oldIsHot == 0 && finalIsHot == 1) {
                    notificationHelper.sendPostTrendingNotification(
                            post.getUserId(), post.getTitle(), post.getId());
                    trendingCount++;
                    log.info("帖子升为热门: postId={}, score={}, title={}",
                            post.getId(), String.format("%.2f", score), post.getTitle());
                }
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("论坛热度计算完成: 更新={}, 新增热门={}, 耗时={}ms",
                    updatedCount, trendingCount, cost);
        } catch (Exception e) {
            log.error("论坛热度计算定时任务异常", e);
        }
    }
}

package com.astronomy.mall.module.forum.task;

import com.astronomy.mall.module.forum.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 论坛定时任务
 *
 * 📌 热度计算（每小时执行一次）:
 *   1. 查 status=2(已发布) + 近7天帖子
 *   2. score = (likes×1 + comments×2 + collects×3) / Math.pow(天数+2, 1.5)
 *   3. 批量UPDATE hot_score, is_hot
 *   4. is_hot 从 0→1 的帖子 → sendPostTrendingNotification（只通知一次）
 *
 * 📌 依赖:
 *   @EnableScheduling 已在 AstronomyMallApplication 上启用
 *   NotificationHelper 在 7.8 通知集成时注入
 */
@Slf4j
@Component
public class ForumScheduler {

    @Autowired
    private PostMapper postMapper;

    // TODO 7.8 注入 NotificationHelper

    /**
     * 每小时计算帖子热度分
     * cron: 秒 分 时 日 月 周
     * "0 0 * * * ?" = 每小时整点执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calcHotScores() {
        // TODO 7.8 实现热度计算
        log.debug("论坛热度计算定时任务执行（骨架，待7.8实现）");
    }
}

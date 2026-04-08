package com.astronomy.mall.module.admin.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 论坛数据统计 VO（管理员后台 7.7 - Tab4 数据统计）
 *
 * 对应接口: GET /api/admin/post/stats
 *
 * 📌 字段说明:
 *   - todayPostCount:    今日发帖数
 *   - todayCommentCount: 今日评论数
 *   - todayActiveUsers:  今日活跃用户数（发帖或评论）
 *   - totalPostCount:    总帖子数（不含管理员删除）
 *   - totalCommentCount: 总评论数
 *   - totalUserCount:    总发帖用户数
 *   - statusDistribution: 帖子状态分布 [{name:"已发布",value:120}, ...]
 *   - last7DaysTrend:    近7天发帖趋势 [{date:"2026-04-01",postCount:10,commentCount:25}, ...]
 *   - pendingCount:      待审核帖子数（兼字段，方便页面右上角直接读）
 */
@Data
public class ForumStatsVO {

    /** 今日发帖数 */
    private Long todayPostCount;

    /** 今日评论数 */
    private Long todayCommentCount;

    /** 今日活跃用户数（发帖+评论 去重） */
    private Long todayActiveUsers;

    /** 总帖子数（已发布+审核中+已拒绝，不含管理员删除） */
    private Long totalPostCount;

    /** 总评论数 */
    private Long totalCommentCount;

    /** 累计发帖用户数 */
    private Long totalUserCount;

    /** 待审核帖子数 */
    private Long pendingCount;

    /** 帖子状态分布（饼图用）[{name:"已发布", value:120}] */
    private List<Map<String, Object>> statusDistribution;

    /** 近7天发帖+评论趋势（折线图用）[{date,postCount,commentCount}] */
    private List<Map<String, Object>> last7DaysTrend;
}

package com.astronomy.mall.module.course.vo;

import lombok.Data;

import java.util.List;

/**
 * 课程视图对象（列表页 + 详情页通用）
 * 对应接口:
 * - GET /api/course/list   → 返回分页列表，不含章节
 * - GET /api/course/{id}  → 返回详情，含 chapters（不含正文）
 *
 * 📌 v5.3 新增字段:
 * - lastChapterTitle: 上次学习章节标题（学习历史页 + 收藏页"继续学习"展示）
 * - completedCount:   已完成章节数（学习历史进度条展示）
 */
@Data
public class CourseVO {

    /** 课程ID */
    private Long id;

    /** 课程标题 */
    private String title;

    /** 副标题/简介 */
    private String subtitle;

    /** 封面图URL */
    private String cover;

    /**
     * 课程类型
     * 0-视频课  1-书本课
     */
    private Integer type;

    /** 类型文本（"视频课" / "书本课"） */
    private String typeText;

    /**
     * 难度等级
     * 1-入门  2-进阶  3-高级
     */
    private Integer difficulty;

    /** 难度文本（"入门" / "进阶" / "高级"） */
    private String difficultyText;

    /**
     * 课程标签（JSON字符串，前端解析为数组展示）
     * 示例: ["深空摄影","望远镜使用"]
     */
    private String tags;

    /** 章节总数 */
    private Integer chapterCount;

    /** 学习人次 */
    private Integer viewCount;

    /** 是否APOD自动同步课程（1=是） */
    private Integer isApodCourse;

    /** 是否火星车自动同步课程（1=是） */
    private Integer isMarsCourse;

    // ========== 登录用户专属字段 ==========

    /**
     * 当前用户是否已收藏（需登录）
     * true-已收藏  false-未收藏  null-未登录
     */
    private Boolean isFavorite;

    /**
     * 上次学习到的章节ID（需登录）
     * 用于「继续学习」按钮直接跳转
     * null 表示浏览过但从未点击章节（"未学习"状态）或未登录
     */
    private Long lastChapterId;

    /**
     * 上次学习到的章节标题（需登录）
     * 📌 v5.3 新增 — 学习历史页「上次学至：xxx章节」展示用
     * null 表示未登录或从未点击章节
     */
    private String lastChapterTitle;

    /**
     * 已完成章节数（需登录）
     * 📌 v5.3 新增 — 学习历史页进度条展示用（completedCount / chapterCount）
     * 0 表示未开始学习
     */
    private Integer completedCount;

    // ========== 详情页专属字段（列表页不填充）==========

    /**
     * 章节目录列表（详情页使用）
     * 只含 id/title/sort/type/duration，不含正文内容
     * 需要正文时单独调用 GET /course/chapter/{chapterId}
     */
    private List<CourseChapterVO> chapters;
}
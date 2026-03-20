package com.astronomy.mall.module.course.vo;

import lombok.Data;

import java.util.List;

/**
 * 课程视图对象（列表页 + 详情页通用）
 * 对应接口:
 * - GET /api/course/list   → 返回分页列表，不含章节
 * - GET /api/course/{id}  → 返回详情，含 chapters（不含正文）
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
     * null 表示从未学习或未登录
     */
    private Long lastChapterId;

    // ========== 详情页专属字段（列表页不填充）==========

    /**
     * 章节目录列表（详情页使用）
     * 只含 id/title/sort/type/duration，不含正文内容
     * 需要正文时单独调用 GET /course/chapter/{chapterId}
     */
    private List<CourseChapterVO> chapters;
}
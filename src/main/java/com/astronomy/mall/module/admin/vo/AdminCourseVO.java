package com.astronomy.mall.module.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台课程管理 VO
 *
 * 课程列表接口返回此 VO（不含 chapters）
 * 章节列表接口 GET /api/admin/course/{id}/chapters 返回 List<ChapterVO>
 */
@Data
public class AdminCourseVO {

    // ===================== 课程基础信息 =====================

    /** 课程 ID */
    private Long id;

    /** 课程标题 */
    private String title;

    /** 副标题 / 简介 */
    private String subtitle;

    /** 封面图 URL */
    private String cover;

    /** 课程类型：0-视频课  1-书本课 */
    private Integer type;

    /** 类型文字（前端展示）：视频课 / 书本课 */
    private String typeText;

    /** 难度：1-入门  2-进阶  3-高级 */
    private Integer difficulty;

    /** 难度文字（前端展示）：入门 / 进阶 / 高级 */
    private String difficultyText;

    /** 课程标签（JSON 字符串，前端用 JSON.parse() 反序列化） */
    private String tags;

    /** 章节数量（冗余字段） */
    private Integer chapterCount;

    /** 学习人次（冗余字段） */
    private Integer viewCount;

    /** 是否 APOD 专属课：0-否  1-是 */
    private Integer isApodCourse;

    /** 是否火星车日志课：0-否  1-是 */
    private Integer isMarsCourse;

    /** 状态：0-草稿  1-已发布 */
    private Integer status;

    /** 状态文字（前端展示）：草稿 / 已发布 */
    private String statusText;

    /** 排序值（越大越靠前） */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ===================== 章节列表（仅 getChapterList 接口返回） =====================

    /** 章节列表，按 sort 升序排列 */
    private List<ChapterVO> chapters;

    // ===================== 内部类：章节 VO =====================

    @Data
    public static class ChapterVO {

        /** 章节 ID */
        private Long id;

        /** 章节标题 */
        private String title;

        /** 内容类型：0-视频  1-图文 */
        private Integer type;

        /** 视频嵌入 URL（type=0 时有值，完整 embed URL） */
        private String videoUrl;

        /**
         * 富文本内容（type=1 时有值）
         * 章节列表接口返回 null（节省流量）
         * 编辑时前端通过章节详情或直接在列表数据中获取
         */
        private String content;

        /** 内容来源：manual / apod / mars_rover */
        private String source;

        /** APOD 对应日期（source=apod 时有值，格式 yyyy-MM-dd） */
        private String apodDate;

        /** APOD 配图 URL（source=apod 时有值） */
        private String apodImage;

        /** 预计学习时长（分钟） */
        private Integer duration;

        /** 章节排序值 */
        private Integer sort;

        /** 创建时间 */
        private LocalDateTime createTime;
    }
}
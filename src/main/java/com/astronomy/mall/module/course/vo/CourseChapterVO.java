package com.astronomy.mall.module.course.vo;

import lombok.Data;

/**
 * 课程章节视图对象
 *
 * 📌 双用途:
 * 1. 章节目录列表（GET /course/{id} 响应中的 chapters 数组）
 *    → 只含目录信息，不含 videoUrl/content 正文（节省带宽）
 *    → 前端左侧章节列表使用
 *
 * 2. 章节正文详情（GET /course/chapter/{chapterId} 响应）
 *    → 含 videoUrl 或 content 正文
 *    → 同时触发 UPSERT tb_course_progress（副作用）
 *    → 前端右侧内容区使用
 */
@Data
public class CourseChapterVO {

    /** 章节ID */
    private Long id;

    /** 所属课程ID */
    private Long courseId;

    /** 章节标题 */
    private String title;

    /**
     * 内容类型
     * 0-视频  1-图文
     */
    private Integer type;

    /** 章节排序 */
    private Integer sort;

    /** 预计学习时长（分钟） */
    private Integer duration;

    /**
     * 是否已完成学习
     * 从 tb_course_progress.completed_chapters 中判断
     * 目录列表时填充，true 时前端显示 ✓ 标记
     */
    private Boolean isCompleted;

    // ========== 正文字段（只在 GET /course/chapter/{id} 时填充）==========

    /**
     * 视频iframe URL（type=0 时有值）
     * 示例: https://www.youtube.com/embed/R5sFYO1bYK0
     * 前端放入 <iframe :src="videoUrl" allowfullscreen>
     */
    private String videoUrl;

    /**
     * 富文本内容（type=1 时有值）
     * TinyMCE 生成的 HTML，前端用 v-html 渲染
     * ⚠️ 注意 XSS，内容均由管理员录入，视为可信
     */
    private String content;

    /**
     * 内容来源
     * manual / apod / mars_rover
     */
    private String source;

    /**
     * APOD 配图URL（source=apod 时有值）
     * 书本课章节顶部展示此图片
     */
    private String apodImage;
}
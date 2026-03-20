package com.astronomy.mall.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程章节表实体
 * 对应数据库表: tb_course_chapter
 *
 * 📌 重要说明:
 * - type=0 视频章节（video_url 为 YouTube/B站 embed URL，前端 iframe 嵌入）
 * - type=1 图文章节（content 为 TinyMCE 生成的 HTML，前端 v-html 渲染）
 * - source='apod' 时，apod_date/apod_image 由 APODSyncScheduler 自动填充
 * - source='mars_rover' 时，由 MarsRoverSyncScheduler 自动填充
 * - source='manual' 时，由管理员手动录入
 */
@Data
@TableName("tb_course_chapter")
public class CourseChapter {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属课程ID */
    private Long courseId;

    /** 章节标题 */
    private String title;

    /**
     * 内容类型（与所属课程 type 保持一致）
     * 0-视频  1-图文
     */
    private Integer type;

    /**
     * 视频iframe URL
     * 示例: https://www.youtube.com/embed/R5sFYO1bYK0
     * type=0 时有值，前端直接放入 <iframe src="...">
     */
    private String videoUrl;

    /**
     * 富文本内容（TinyMCE HTML）
     * type=1 时有值，前端 v-html 渲染
     * ⚠️ 使用 MEDIUMTEXT 字段，可存储大量图文内容
     */
    private String content;

    /**
     * 内容来源
     * manual     - 管理员手动录入
     * apod       - NASA APOD 定时同步（APODSyncScheduler）
     * mars_rover - 火星车照片同步（MarsRoverSyncScheduler）
     */
    private String source;

    /**
     * APOD 对应日期
     * source='apod' 时有值，格式: 2026-03-19
     */
    private LocalDate apodDate;

    /**
     * APOD 配图URL
     * source='apod' 时有值，hdurl 优先
     */
    private String apodImage;

    /** 预计学习时长（分钟） */
    private Integer duration;

    /** 章节排序（越小越靠前，从1开始） */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
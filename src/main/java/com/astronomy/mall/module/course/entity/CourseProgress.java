package com.astronomy.mall.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程学习进度表实体
 * 对应数据库表: tb_course_progress
 * （原 tb_user_course 完全重新设计）
 *
 * 📌 重要说明:
 * - UNIQUE KEY uk_user_course (user_id, course_id) 保证每用户每课程只有一条记录
 * - completed_chapters 是 JSON 数组，存储所有已完成的章节 ID
 * - 完课检测: completed_chapters.size() >= course.chapter_count
 *   ⚠️ 但 APOD课 和 火星车课 永远不触发完课通知
 * - 使用 UPSERT (INSERT ON DUPLICATE KEY UPDATE) 操作，保证原子性
 */
@Data
@TableName("tb_course_progress")
public class CourseProgress {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 课程ID */
    private Long courseId;

    /**
     * 上次学习的章节ID
     * 用于「继续学习」按钮，进入课程时直接跳转此章节
     * null 表示从未学习
     */
    private Long lastChapterId;

    /**
     * 已完成的章节ID列表（JSON数组）
     * 示例: "[1, 2, 5, 8]"
     * 每次访问新章节时，将该章节ID加入此数组（若不存在）
     */
    private String completedChapters;

    /** 最近学习时间 */
    private LocalDateTime lastLearnTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
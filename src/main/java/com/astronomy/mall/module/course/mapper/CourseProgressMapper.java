package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.entity.CourseProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程学习进度Mapper
 * 对应表: tb_course_progress
 *
 * 📌 UPSERT说明:
 * 使用 INSERT INTO ... ON DUPLICATE KEY UPDATE 实现原子性更新进度
 * UNIQUE KEY uk_user_course (user_id, course_id) 保证唯一
 */
@Mapper
public interface CourseProgressMapper extends BaseMapper<CourseProgress> {

    /**
     * UPSERT 学习进度
     * 若记录不存在则插入，存在则更新 last_chapter_id / completed_chapters / last_learn_time
     *
     * @param userId             用户ID
     * @param courseId           课程ID
     * @param chapterId          当前章节ID
     * @param completedChapters  更新后的已完成章节JSON数组字符串
     */
    void upsertProgress(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId,
            @Param("completedChapters") String completedChapters
    );
}
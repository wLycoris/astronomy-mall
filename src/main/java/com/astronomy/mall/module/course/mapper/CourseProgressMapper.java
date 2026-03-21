package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.entity.CourseProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程学习进度Mapper
 * 对应表: tb_course_progress
 *
 * 📌 UPSERT说明:
 * 使用 INSERT INTO ... ON DUPLICATE KEY UPDATE 实现原子性更新进度
 * UNIQUE KEY uk_user_course (user_id, course_id) 保证唯一
 *
 * 📌 v5.3 新增:
 * - insertIgnoreVisit: 仅建立访问记录（不覆盖已有进度），用于课程详情页进入时埋点
 *   使课程在学习历史中出现，lastChapterId=null 表示「浏览过但未学习」
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

    /**
     * 仅创建访问记录（INSERT IGNORE，不覆盖已有进度）
     *
     * 📌 使用场景:
     * - getCourseDetail() 登录用户进入详情页时调用
     * - 保证课程出现在学习历史中，即使用户未点击任何章节
     * - lastChapterId=NULL + completed_chapters='[]' 对应前端「未学习」状态
     * - 若已有进度记录（已点击过章节），INSERT IGNORE 自动跳过，不影响已有数据
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     */
    @Insert("INSERT IGNORE INTO tb_course_progress " +
            "(user_id, course_id, completed_chapters, last_learn_time) " +
            "VALUES (#{userId}, #{courseId}, '[]', NOW())")
    void insertIgnoreVisit(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
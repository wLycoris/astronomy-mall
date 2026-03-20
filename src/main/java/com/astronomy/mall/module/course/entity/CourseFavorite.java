package com.astronomy.mall.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程收藏表实体
 * 对应数据库表: tb_course_favorite
 *
 * 📌 重要说明:
 * - UNIQUE KEY uk_user_course (user_id, course_id) 保证不重复收藏
 * - 收藏/取消收藏使用幂等接口 POST /course/favorite/toggle/{courseId}
 * - 收藏用户在课程有新章节时会收到通知（COURSE_CHAPTER_ADDED）
 * - 收藏APOD课的用户每天凌晨会收到通知（COURSE_APOD_UPDATED）
 */
@Data
@TableName("tb_course_favorite")
public class CourseFavorite {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 课程ID */
    private Long courseId;

    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
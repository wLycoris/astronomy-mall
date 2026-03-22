package com.astronomy.mall.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程评价实体类
 * 对应表: tb_course_review
 *
 * 状态说明:
 *   status = 1: 正常
 *   status = 0: 管理员逻辑删除
 *
 * ⚠️ 表中无 deleted 字段，通过 status=0 实现软删除，不使用 @TableLogic
 * ⚠️ 每门课每人只能评价一次，由 Service 层 getUserReview() 保证
 */
@Data
@TableName("tb_course_review")
public class CourseReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID 📌关联 tb_course.id */
    private Long courseId;

    /** 用户ID 📌关联 tb_user.id */
    private Long userId;

    /** 评分(1-5星) */
    private Integer rating;

    /** 评价文字内容（可为空） */
    private String content;

    /** 点赞数（预留，本期不实现点赞功能） */
    private Integer likeCount;

    /**
     * 状态
     *   1 - 正常（默认）
     *   0 - 管理员逻辑删除
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
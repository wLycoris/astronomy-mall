package com.astronomy.mall.module.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程主表实体
 * 对应数据库表: tb_course
 *
 * 📌 重要说明:
 * - type=0 视频课，type=1 书本课
 * - is_apod_course=1 时，章节由 APODSyncScheduler 每天凌晨2点自动同步
 * - is_mars_course=1 时，章节由 MarsRoverSyncScheduler 每天凌晨2:30自动同步
 * - 这两类课程永远不会触发「完课」通知（因为章节每天递增）
 * - deleted=1 为逻辑删除（@TableLogic）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_course")
public class Course {

    /** 课程ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程标题 */
    private String title;

    /** 副标题/简介 */
    private String subtitle;

    /** 封面图URL */
    private String cover;

    /**
     * 课程类型
     * 0-视频课（iframe嵌入YouTube/B站）
     * 1-书本课（TinyMCE富文本）
     */
    private Integer type;

    /**
     * 难度等级
     * 1-入门  2-进阶  3-高级
     */
    private Integer difficulty;

    /**
     * 课程标签（JSON数组）
     * 示例: ["深空摄影","望远镜使用","哈勃望远镜"]
     * 📌 与 tb_product.tags 格式一致，用于购买商品后推荐相关课程
     */
    private String tags;

    /** 章节数量（冗余字段，新增/删除章节时同步更新） */
    private Integer chapterCount;

    /** 学习人次（冗余字段） */
    private Integer viewCount;

    /**
     * 是否NASA APOD专属课程
     * 1=是，章节由 APODSyncScheduler 每天凌晨2点自动同步
     * ⚠️ 完课检测时必须排除此类课程
     */
    private Integer isApodCourse;

    /**
     * 是否火星探测车日志课程
     * 1=是，章节由 MarsRoverSyncScheduler 每天凌晨2:30自动同步
     * ⚠️ 完课检测时必须排除此类课程
     */
    private Integer isMarsCourse;

    /**
     * 课程状态
     * 0-草稿（不对外展示）
     * 1-已发布
     */
    private Integer status;

    /** 排序值（越大越靠前） */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记（0-正常 1-已删除），MyBatis-Plus @TableLogic 自动过滤 */
    @TableLogic
    private Integer deleted;
}
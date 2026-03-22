package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.entity.CourseReview;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 课程评价 Mapper
 * 文件路径: com.astronomy.mall.module.course.mapper.CourseReviewMapper
 *
 * 简单查询用 @Select 注解；
 * 管理员多条件查询在 resources/mapper/CourseReviewMapper.xml 中实现
 */
@Mapper
public interface CourseReviewMapper extends BaseMapper<CourseReview> {

    /**
     * 查询用户对某门课程的评价（status=1）
     * 用途: 提交前判断是否重复；展示我的评价
     */
    @Select("SELECT * FROM tb_course_review " +
            "WHERE course_id = #{courseId} AND user_id = #{userId} AND status = 1 LIMIT 1")
    CourseReview getUserReview(@Param("courseId") Long courseId,
                               @Param("userId") Long userId);

    /**
     * 用户端：课程评价列表（分页，含用户昵称/头像，按时间倒序）
     * 只返回 status=1 的正常评价
     */
    @Select("SELECT r.id, r.course_id AS courseId, r.user_id AS userId, " +
            "r.rating, r.content, r.like_count AS likeCount, r.create_time AS createTime, " +
            "u.nickname, u.avatar " +
            "FROM tb_course_review r " +
            "LEFT JOIN tb_user u ON r.user_id = u.id " +
            "WHERE r.course_id = #{courseId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC")
    List<Map<String, Object>> selectUserReviewPage(Page<Map<String, Object>> page,
                                                   @Param("courseId") Long courseId);

    /**
     * 管理员端：评价列表（多条件可选筛选）
     * SQL 在 CourseReviewMapper.xml 中实现
     */
    List<Map<String, Object>> selectAdminReviewPage(Page<Map<String, Object>> page,
                                                    @Param("courseId") Long courseId,
                                                    @Param("rating") Integer rating,
                                                    @Param("keyword") String keyword);

    /** 统计全部正常评价总数（status=1） */
    @Select("SELECT COUNT(*) FROM tb_course_review WHERE status = 1")
    Integer countTotal();

    /**
     * 统计本周新增评价数（status=1）
     * WEEKDAY(NOW())=0 是周一，减去该天数得到本周一 00:00:00
     */
    @Select("SELECT COUNT(*) FROM tb_course_review " +
            "WHERE status = 1 " +
            "AND create_time >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(NOW()) DAY)")
    Integer countThisWeek();

    /** 计算全部评价平均评分（status=1），无数据时返回 null */
    @Select("SELECT AVG(rating) FROM tb_course_review WHERE status = 1")
    Double avgRating();

    /**
     * 查询用户的课程评价列表（含课程标题/封面，按时间倒序）
     * 用于「我的评价」页面
     */
    @Select("SELECT r.id, r.course_id AS courseId, r.rating, r.content, " +
            "r.create_time AS createTime, r.update_time AS updateTime, " +
            "c.title AS courseTitle, c.cover AS courseCover " +
            "FROM tb_course_review r " +
            "LEFT JOIN tb_course c ON r.course_id = c.id " +
            "WHERE r.user_id = #{userId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC")
    List<Map<String, Object>> selectMyReviewPage(Page<Map<String, Object>> page,
                                                 @Param("userId") Long userId);

    /**
     * 更新评价内容（编辑）
     */
    @Update("UPDATE tb_course_review SET rating = #{rating}, content = #{content}, " +
            "update_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND status = 1")
    int updateReview(@Param("id") Long id, @Param("userId") Long userId,
                     @Param("rating") Integer rating, @Param("content") String content);
}
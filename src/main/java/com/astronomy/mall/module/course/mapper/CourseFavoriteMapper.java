package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.entity.CourseFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程收藏Mapper
 * 对应表: tb_course_favorite
 */
@Mapper
public interface CourseFavoriteMapper extends BaseMapper<CourseFavorite> {

    /**
     * 查询收藏了某课程的所有用户ID
     * 用于新增章节时批量发送通知
     *
     * @param courseId 课程ID
     * @return 收藏该课程的用户ID列表
     */
    @Select("SELECT user_id FROM tb_course_favorite WHERE course_id = #{courseId}")
    List<Long> selectUserIdsByCourseId(@Param("courseId") Long courseId);

    /**
     * 查询某用户是否收藏了某课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     * @return 收藏记录（null表示未收藏）
     */
    @Select("SELECT * FROM tb_course_favorite WHERE user_id = #{userId} AND course_id = #{courseId} LIMIT 1")
    CourseFavorite selectByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.dto.CourseQueryDTO;
import com.astronomy.mall.module.course.entity.Course;
import com.astronomy.mall.module.course.vo.CourseVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程Mapper
 * 继承 BaseMapper 获得基础 CRUD
 * 自定义方法见 CourseMapper.xml
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程列表（含多标签AND筛选）
     *
     * @param page   MyBatis-Plus 分页对象
     * @param dto    查询条件（type/difficulty/keyword/tags）
     * @param userId 当前登录用户ID（null表示未登录，不附加isFavorite字段）
     * @return 分页课程列表（含 isFavorite 字段）
     *
     * 📌 多标签AND筛选实现:
     * tags 字段存储 JSON 数组，如 '["深空摄影","望远镜使用"]'
     * 使用 JSON_CONTAINS(tags, JSON_QUOTE(tag)) 检测每个标签是否存在
     * 多个标签用 AND 连接，取交集
     */
    IPage<CourseVO> selectCoursePageWithFavorite(
            Page<CourseVO> page,
            @Param("dto") CourseQueryDTO dto,
            @Param("userId") Long userId
    );
}
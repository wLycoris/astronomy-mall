package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.dto.CourseQueryDTO;
import com.astronomy.mall.module.course.entity.Course;
import com.astronomy.mall.module.course.vo.CourseVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 课程Mapper
 * 继承 BaseMapper 获得基础 CRUD
 * 自定义方法见 CourseMapper.xml
 *
 * 📌 版本变更说明 (5.2):
 * - 新增 incrChapterCount(courseId)  章节数 +1
 *   供 APODSyncScheduler / MarsRoverSyncScheduler 插入新章节后维护 chapter_count 冗余字段
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

    /**
     * 课程章节数 +1（原子自增，线程安全）
     *
     * 📌 使用场景:
     * - APODSyncScheduler.syncTodayApod()         每日 APOD 同步成功后调用
     * - MarsRoverSyncScheduler.syncLatestMarsPhotos() 每日火星车照片同步成功后，每新增一个章节调用一次
     * - AdminCourseServiceImpl.addChapter()       管理员手动新增章节后调用
     *
     * ⚠️ 使用 SQL 层自增（chapter_count = chapter_count + 1）而非先 SELECT 再 UPDATE，
     *    避免并发场景下的计数不一致问题
     *
     * @param courseId 课程ID（tb_course.id）
     */
    @Update("UPDATE tb_course SET chapter_count = chapter_count + 1 WHERE id = #{courseId} AND deleted = 0")
    void incrChapterCount(@Param("courseId") Long courseId);
}
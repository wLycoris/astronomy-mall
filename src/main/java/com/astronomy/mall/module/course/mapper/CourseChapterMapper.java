package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.entity.CourseChapter;
import com.astronomy.mall.module.course.vo.CourseChapterVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程章节Mapper
 * 对应表: tb_course_chapter
 *
 * 📌 版本变更说明 (5.2):
 * - 新增 getMaxSort(courseId)   获取指定课程下章节的最大排序值
 *   供 APODSyncScheduler / MarsRoverSyncScheduler 新增章节时追加到末尾
 */
@Mapper
public interface CourseChapterMapper extends BaseMapper<CourseChapter> {

    /**
     * 查询课程的章节目录（不含正文，只有 id/title/sort/type/duration）
     * 用于课程详情页左侧章节目录渲染，节省带宽
     *
     * @param courseId          课程ID
     * @param completedChapters 已完成章节ID JSON数组字符串（如"[1,2,3]"），null时全部isCompleted=false
     * @return 章节目录列表（按 sort ASC 排序）
     */
    List<CourseChapterVO> selectChapterOutlineList(
            @Param("courseId") Long courseId,
            @Param("completedChapters") String completedChapters
    );

    /**
     * 获取指定课程下所有章节的最大排序值
     *
     * 📌 使用场景:
     * - APODSyncScheduler 每日新增 APOD 章节时调用，新章节 sort = maxSort + 1
     * - MarsRoverSyncScheduler 新增火星车照片章节时调用，同上
     * - 防止新章节 sort 值重复或跳序
     *
     * @param courseId 课程ID
     * @return 最大 sort 值；若该课程下还没有任何章节，则返回 null（调用方判空后默认取 0）
     */
    @Select("SELECT MAX(sort) FROM tb_course_chapter WHERE course_id = #{courseId}")
    Integer getMaxSort(@Param("courseId") Long courseId);
}
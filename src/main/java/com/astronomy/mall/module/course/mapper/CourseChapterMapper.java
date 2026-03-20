package com.astronomy.mall.module.course.mapper;

import com.astronomy.mall.module.course.entity.CourseChapter;
import com.astronomy.mall.module.course.vo.CourseChapterVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程章节Mapper
 * 对应表: tb_course_chapter
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
}
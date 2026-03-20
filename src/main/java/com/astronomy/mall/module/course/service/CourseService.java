package com.astronomy.mall.module.course.service;

import com.astronomy.mall.module.course.dto.CourseQueryDTO;
import com.astronomy.mall.module.course.vo.CourseChapterVO;
import com.astronomy.mall.module.course.vo.CourseVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 课程 Service 接口
 * 对应用户端3个核心接口:
 *   GET /api/course/list              → getCourseList
 *   GET /api/course/{id}              → getCourseDetail
 *   GET /api/course/chapter/{chId}    → getChapter（副作用: 记录进度）
 */
public interface CourseService {

    /**
     * 分页查询课程列表
     *
     * @param dto    查询条件（含多标签AND筛选）
     * @param userId 当前用户ID（null=未登录，不附加收藏/进度字段）
     * @return 分页课程列表
     *
     * 📌 标签AND筛选:
     * dto.tags 为逗号分隔字符串，在 Service 层 split(",") 后存入 dto.tagList
     * Mapper层对每个 tag 做 JSON_CONTAINS 检测，AND 取交集
     */
    IPage<CourseVO> getCourseList(CourseQueryDTO dto, Long userId);

    /**
     * 获取课程详情（含章节目录，不含章节正文）
     *
     * @param courseId 课程ID
     * @param userId   当前用户ID（null=未登录）
     * @return 课程详情VO（含 chapters 章节目录列表、isFavorite、lastChapterId）
     */
    CourseVO getCourseDetail(Long courseId, Long userId);

    /**
     * 获取章节完整内容（含正文）
     * 副作用（登录用户）:
     *   - UPSERT tb_course_progress（记录 last_chapter_id + 追加 completed_chapters）
     *   - 检测完课（排除 APOD课/火星课），完课时调用 sendCourseCompletedNotification
     *
     * @param chapterId 章节ID
     * @param userId    当前用户ID（null=未登录，仅返回内容不记录进度）
     * @return 章节完整内容VO（含 videoUrl 或 content）
     */
    CourseChapterVO getChapter(Long chapterId, Long userId);

    /**
     * 切换课程收藏状态（幂等）
     * 已收藏 → 取消收藏
     * 未收藏 → 收藏
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     * @return true=当前已收藏，false=当前已取消
     */
    boolean toggleFavorite(Long courseId, Long userId);

    /**
     * 查询我的课程收藏列表（分页）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页收藏课程列表
     */
    IPage<CourseVO> getMyFavoriteList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询我的学习历史（按最近学习时间倒序）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页学习历史列表
     */
    IPage<CourseVO> getMyHistory(Long userId, Integer pageNum, Integer pageSize);
}
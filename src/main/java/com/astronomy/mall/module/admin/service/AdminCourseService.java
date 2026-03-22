package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.ApodSyncDTO;
import com.astronomy.mall.module.admin.dto.ChapterCreateDTO;
import com.astronomy.mall.module.admin.dto.CourseCreateDTO;
import com.astronomy.mall.module.admin.vo.AdminCourseReviewVO;
import com.astronomy.mall.module.admin.vo.AdminCourseVO;
import com.astronomy.mall.module.nasa.vo.ApodVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 后台课程管理 Service 接口
 *
 * 11个管理员端接口对应的业务方法 + 1个内部方法（insertOneApodDay）
 * v5.6 新增2个评价管理方法: getCourseReviews / deleteCourseReview
 */
public interface AdminCourseService {

    // ===================== 课程管理（5个）=====================

    /**
     * 课程列表（分页 + 关键词 + type + status 筛选）
     */
    Page<AdminCourseVO> getCourseList(Integer pageNum, Integer pageSize,
                                      String keyword, Integer type, Integer status);

    /**
     * 新增课程（默认 status=0 草稿）
     *
     * @return 新课程 ID
     */
    Long addCourse(CourseCreateDTO dto);

    /**
     * 编辑课程基本信息
     */
    void updateCourse(Long id, CourseCreateDTO dto);

    /**
     * 删除课程（逻辑删除，deleted=1）
     */
    void deleteCourse(Long id);

    /**
     * 发布 / 下架课程（status 0↔1）
     */
    void updateCourseStatus(Long id, Integer status);

    // ===================== 章节管理（5个）=====================

    /**
     * 获取课程章节列表（按 sort 升序）
     */
    List<AdminCourseVO.ChapterVO> getChapterList(Long courseId);

    /**
     * 新增章节
     * 📌 成功后异步通知所有收藏该课程的用户
     *
     * @return 新章节 ID
     */
    Long addChapter(ChapterCreateDTO dto);

    /**
     * 编辑章节
     */
    void updateChapter(Long id, ChapterCreateDTO dto);

    /**
     * 删除章节（物理删除，同步将 tb_course.chapter_count - 1）
     */
    void deleteChapter(Long id);

    /**
     * 批量更新章节排序（拖拽排序）
     *
     * @param sortList [{id:1, sort:0}, {id:2, sort:1}, ...]
     */
    void sortChapters(List<Map<String, Object>> sortList);

    // ===================== APOD 批量同步（1个）=====================

    /**
     * 手动触发 APOD 历史数据批量同步（入口，无事务）
     *
     * 每天通过 insertOneApodDay() 独立提交（REQUIRES_NEW），
     * NASA API 500 只跳过当天，不影响其他天的已提交数据。
     *
     * @param dto 同步日期范围（单次 ≤ 60 天）
     * @return 本次新增章节数量
     */
    int syncApodRange(ApodSyncDTO dto);

    /**
     * 单天 APOD 数据入库（独立事务 REQUIRES_NEW）
     *
     * 📌 此方法暴露在接口层，是为了让 Spring AOP 代理能够拦截到
     *    @Transactional(REQUIRES_NEW)。
     *    syncApodRange() 通过 self.insertOneApodDay() 调用（self = 代理对象）。
     *
     * ⚠️ 不建议在 syncApodRange() 以外的地方直接调用此方法。
     *
     * @param apodCourseId APOD 专属课程 ID
     * @param date         当天日期
     * @param apod         NASA APOD 数据
     */
    void insertOneApodDay(Long apodCourseId, LocalDate date, ApodVO apod);

    // ===================== 5.6 课程评价（2个）=====================

    /**
     * 管理员端：课程评价列表（分页 + 多条件筛选）
     *
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @param courseId  课程ID（null=全部）
     * @param rating    星级（null或0=全部，1-5精确匹配）
     * @param keyword   用户名/昵称关键词（null=不过滤）
     */
    Page<AdminCourseReviewVO> getCourseReviews(int pageNum, int pageSize,
                                               Long courseId, Integer rating, String keyword);

    /**
     * 管理员端：逻辑删除课程评价（status 置为 0）
     *
     * @param id 评价ID
     */
    void deleteCourseReview(Long id);
}
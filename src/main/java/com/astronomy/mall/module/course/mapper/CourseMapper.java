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

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程Mapper
 * 继承 BaseMapper 获得基础 CRUD
 * 自定义方法见 CourseMapper.xml
 *
 * 📌 版本变更说明 (5.2):
 * - 新增 incrChapterCount(courseId)  章节数 +1
 *   供 APODSyncScheduler / MarsRoverSyncScheduler 插入新章节后维护 chapter_count 冗余字段
 *
 * 📌 版本变更说明 (5.4):
 * - 新增 getUserRecentOrderProductTags(userId, since)
 *   查询用户近3个月已购买订单中商品的 tags JSON 字符串列表
 * - 新增 getRecommendByTags(userId, tags, limit)
 *   根据标签列表 LIKE 匹配课程，排除已有学习进度的课程，按 view_count 倒序
 * - 新增 getHotCourses(userId, limit)
 *   热门课程兜底（无标签命中时使用），按 view_count 倒序
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
     * - APODSyncScheduler.syncTodayApod()             每日 APOD 同步成功后调用
     * - MarsRoverSyncScheduler.syncLatestMarsPhotos() 每日火星车同步成功后调用
     * - AdminCourseServiceImpl.addChapter()           管理员手动新增章节后调用
     *
     * @param courseId 课程ID（tb_course.id）
     */
    @Update("UPDATE tb_course SET chapter_count = chapter_count + 1 WHERE id = #{courseId} AND deleted = 0")
    void incrChapterCount(@Param("courseId") Long courseId);

    // ==================== 5.4 购买商品→推荐课程 ====================

    /**
     * 查询用户近3个月已购买订单中商品的 tags JSON 字符串列表
     *
     * 查询逻辑:
     * - JOIN 链路: tb_order → tb_order_item → tb_product
     * - 过滤: 非取消订单(status!=4) + 3个月内 + 商品有tags + deleted=0
     * - DISTINCT 去重同一商品的重复 tags 字符串
     * - 返回 List<String>，每个元素是一个 JSON 数组字符串，如 '["深空摄影","望远镜使用"]'
     *
     * @param userId 用户ID
     * @param since  起始时间（3个月前，由 Service 层计算传入）
     * @return 商品 tags JSON 字符串列表（可能包含多条，由 Service 层解析合并去重）
     */
    List<String> getUserRecentOrderProductTags(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    /**
     * 根据标签列表 LIKE 匹配课程（5.4 推荐主查询）
     *
     * 查询逻辑:
     * - foreach 遍历 tags 列表，每个 tag 形成 LIKE '%tag%' 条件（OR 关系）
     * - LEFT JOIN tb_course_favorite 附带当前用户收藏状态
     * - NOT IN 子查询排除已有 tb_course_progress 记录的课程
     * - status=1(已发布) + deleted=0(未删除)
     * - 按 view_count 倒序，返回最多 limit 个
     *
     * @param userId 用户ID（用于排除已学习课程 + 附带收藏状态；null=不过滤）
     * @param tags   标签关键词列表（从购买商品 tags JSON 中提取合并去重后的列表）
     * @param limit  最多返回条数（5.4 固定传6）
     * @return 匹配的课程 VO 列表
     */
    List<CourseVO> getRecommendByTags(
            @Param("userId") Long userId,
            @Param("tags") List<String> tags,
            @Param("limit") int limit);

    /**
     * 热门课程兜底（5.4 无标签命中时使用）
     *
     * 查询逻辑:
     * - 不做标签过滤，纯按 view_count 倒序
     * - 同样排除已有 tb_course_progress 记录的课程（保证推荐的是"还没学的"）
     * - status=1(已发布) + deleted=0(未删除)
     *
     * @param userId 用户ID（null=不过滤已学习课程，如未登录时兜底使用）
     * @param limit  最多返回条数（5.4 固定传6）
     * @return 热门课程 VO 列表
     */
    List<CourseVO> getHotCourses(
            @Param("userId") Long userId,
            @Param("limit") int limit);
}
package com.astronomy.mall.module.course.service;

import com.astronomy.mall.module.course.dto.CourseQueryDTO;
import com.astronomy.mall.module.course.dto.CourseReviewSubmitDTO;
import com.astronomy.mall.module.course.vo.CourseChapterVO;
import com.astronomy.mall.module.course.vo.CourseReviewVO;
import com.astronomy.mall.module.course.vo.CourseVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 课程 Service 接口
 * 对应用户端接口:
 *   GET /api/course/list              → getCourseList
 *   GET /api/course/{id}              → getCourseDetail
 *   GET /api/course/chapter/{chId}    → getChapter（副作用: 记录进度）
 *   POST/api/course/favorite/toggle/{id} → toggleFavorite
 *   GET /api/course/favorite/list     → getMyFavoriteList
 *   GET /api/course/history           → getMyHistory
 *   GET /api/course/recommend         → getRecommendCourses（5.4新增）
 *   POST /api/course/{id}/review      → submitCourseReview（5.6新增）
 *   GET /api/course/{id}/reviews      → getCourseReviews（5.6新增）
 *   GET /api/course/{id}/review/my    → getMyReview（5.6新增）
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

    // ==================== 5.4 购买商品→推荐课程 ====================

    /**
     * 根据用户近3个月购买商品的标签推荐相关课程
     *
     * 推荐逻辑（7步兜底保障）:
     *  Step 1: userId=null（未登录）      → 直接热门兜底
     *  Step 2: 查近3个月非取消订单商品tags
     *  Step 3: 无购买记录                → 热门兜底
     *  Step 4: 解析商品 tags JSON        → Set 合并去重
     *  Step 5: 合并后标签集为空          → 热门兜底
     *  Step 6: LIKE 匹配 tb_course.tags（OR关系，命中任意tag即入选）
     *  Step 7: 无命中课程               → 热门兜底
     *  最终: 返回最多6个，已学习的课程自动排除
     *
     * 前端显示条件（CourseList.vue）:
     *  v-if="getToken() && recommendList.length > 0"
     *  → 未登录：前端不调用接口，区块不显示
     *  → 已登录但接口返回空：区块不显示
     *  → 已登录且有数据：显示「为你推荐」横向滑动卡片
     *
     * @param userId 当前用户ID（null=未登录）
     * @return 推荐课程列表，最多6个 CourseVO
     */
    List<CourseVO> getRecommendCourses(Long userId);

    // ==================== 5.6 课程评价 ====================

    /**
     * 提交课程评价
     * 校验顺序: 课程存在 → 有学习进度 → 未重复评价
     *
     * @param courseId 课程ID
     * @param userId   当前登录用户ID
     * @param dto      评价内容（rating必传，content可选）
     * @throws com.astronomy.mall.common.exception.BusinessException 课程不存在 / 未学习该课程 / 已评过
     */
    void submitCourseReview(Long courseId, Long userId, CourseReviewSubmitDTO dto);

    /**
     * 课程评价列表（用户端，分页，按时间倒序，只返回 status=1）
     * GET /api/course/{courseId}/reviews
     *
     * @param courseId 课程ID
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     */
    Page<CourseReviewVO> getCourseReviews(Long courseId, int pageNum, int pageSize);

    /**
     * 查询当前用户对该课程的评价（判断是否已评 + 返回内容）
     * GET /api/course/{courseId}/review/my
     *
     * @param courseId 课程ID
     * @param userId   当前登录用户ID
     * @return 已评时返回 CourseReviewVO；未评时返回 null
     */
    CourseReviewVO getMyReview(Long courseId, Long userId);

    /**
     * 查询用户的课程评价列表（「我的评价」页面）
     */
    Page<CourseReviewVO> getMyReviewList(Long userId, int pageNum, int pageSize);

    /**
     * 编辑已有评价（只能改自己的，status=1才能改）
     */
    void updateCourseReview(Long courseId, Long userId, CourseReviewSubmitDTO dto);
}
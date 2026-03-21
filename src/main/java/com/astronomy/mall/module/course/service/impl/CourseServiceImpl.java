package com.astronomy.mall.module.course.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.course.dto.CourseQueryDTO;
import com.astronomy.mall.module.course.entity.Course;
import com.astronomy.mall.module.course.entity.CourseFavorite;
import com.astronomy.mall.module.course.entity.CourseChapter;
import com.astronomy.mall.module.course.entity.CourseProgress;
import com.astronomy.mall.module.course.mapper.*;
import com.astronomy.mall.module.course.service.CourseService;
import com.astronomy.mall.module.course.vo.CourseChapterVO;
import com.astronomy.mall.module.course.vo.CourseVO;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程 Service 实现
 *
 * 📌 v5.3 修复说明:
 * 1. getCourseDetail(): 登录用户进入详情页时调用 insertIgnoreVisit() 埋点，
 *    保证课程出现在学习历史列表中（即使未点击任何章节，显示"未学习"状态）
 *
 * 2. getMyHistory(): 新增 lastChapterTitle（JOIN chapter表查名称）
 *    和 completedCount（解析 completed_chapters JSON数组长度），
 *    供 CourseHistory.vue 进度条和"上次学至"展示
 *
 * 3. getMyFavoriteList(): 新增 lastChapterId / lastChapterTitle / completedCount，
 *    供 CourseFavorite.vue 「继续学习/开始学习」按钮判断
 *
 * 4. fix: item.courseId → CourseVO.id（VO 主键字段名为 id 而非 courseId）
 *
 * 📌 v5.4 新增:
 * 5. getRecommendCourses(userId)：
 *    根据用户近3个月购买商品的 tags 匹配课程 tags 推荐课程。
 *    7步兜底逻辑保障始终有结果，已学习的课程自动排除，最多返回6个。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseProgressMapper courseProgressMapper;
    private final CourseFavoriteMapper courseFavoriteMapper;
    private final NotificationHelper notificationHelper;

    // ============================== 课程列表 ==============================

    @Override
    public IPage<CourseVO> getCourseList(CourseQueryDTO dto, Long userId) {
        // 处理多标签AND筛选：将逗号分隔的字符串 → List<String>，存入 dto.tagList
        if (StringUtils.hasText(dto.getTags())) {
            List<String> tagList = Arrays.stream(dto.getTags().split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
            dto.setTagList(tagList);
        }

        Page<CourseVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        return courseMapper.selectCoursePageWithFavorite(page, dto, userId);
    }

    // ============================== 课程详情 ==============================

    @Override
    public CourseVO getCourseDetail(Long courseId, Long userId) {
        // 1. 查主表
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getDeleted() == 1) {
            throw new BusinessException("课程不存在");
        }
        if (course.getStatus() == 0) {
            throw new BusinessException("课程暂未发布");
        }

        // 2. 转为VO，填充基础信息
        CourseVO vo = buildCourseVO(course);

        // 3. 登录用户：埋点访问记录 + 查进度/收藏
        String completedChapters = null;
        if (userId != null) {
            // ✅ v5.3 新增：INSERT IGNORE 建立访问记录
            // 若已有进度则自动跳过，保证课程出现在学习历史（lastChapterId=null="未学习"）
            try {
                courseProgressMapper.insertIgnoreVisit(userId, courseId);
            } catch (Exception e) {
                // 埋点失败不影响详情返回，静默处理
                log.warn("[CourseService] insertIgnoreVisit 失败 userId={} courseId={}: {}",
                        userId, courseId, e.getMessage());
            }

            // 查进度
            CourseProgress progress = courseProgressMapper.selectOne(
                    new LambdaQueryWrapper<CourseProgress>()
                            .eq(CourseProgress::getUserId, userId)
                            .eq(CourseProgress::getCourseId, courseId)
            );
            if (progress != null) {
                vo.setLastChapterId(progress.getLastChapterId());
                completedChapters = progress.getCompletedChapters();
            }

            // 查收藏状态
            CourseFavorite favorite = courseFavoriteMapper.selectByUserAndCourse(userId, courseId);
            vo.setIsFavorite(favorite != null);
        }

        // 4. 查章节目录（不含正文，含 isCompleted 标记）
        List<CourseChapterVO> chapters =
                courseChapterMapper.selectChapterOutlineList(courseId, completedChapters);
        vo.setChapters(chapters);

        // 5. view_count +1
        courseMapper.updateById(Course.builder().id(courseId)
                .viewCount(course.getViewCount() + 1).build());

        return vo;
    }

    // ============================== 章节内容（含进度记录）==============================

    @Override
    public CourseChapterVO getChapter(Long chapterId, Long userId) {
        // 1. 查章节
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException("章节不存在");
        }

        // 2. 转为VO（包含 videoUrl / content 正文）
        CourseChapterVO vo = buildChapterVO(chapter);

        // 3. 未登录用户：只返回内容，不记录进度
        if (userId == null) {
            return vo;
        }

        // 4. 登录用户：UPSERT 进度
        Long courseId = chapter.getCourseId();
        upsertProgress(userId, courseId, chapterId);

        return vo;
    }

    /**
     * UPSERT 学习进度
     * 1. 查旧进度（判断该章节是否已完成）
     * 2. 若未完成，追加到 completed_chapters 数组
     * 3. 执行 UPSERT SQL
     * 4. 完课检测（排除APOD/火星课）
     */
    private void upsertProgress(Long userId, Long courseId, Long chapterId) {
        try {
            // 查旧进度
            CourseProgress oldProgress = courseProgressMapper.selectOne(
                    new LambdaQueryWrapper<CourseProgress>()
                            .eq(CourseProgress::getUserId, userId)
                            .eq(CourseProgress::getCourseId, courseId)
            );

            // 计算新的 completed_chapters
            JSONArray completedArr;
            if (oldProgress != null && StringUtils.hasText(oldProgress.getCompletedChapters())) {
                completedArr = JSON.parseArray(oldProgress.getCompletedChapters());
            } else {
                completedArr = new JSONArray();
            }

            // 若当前章节不在已完成列表中，则追加
            boolean alreadyCompleted = false;
            for (int i = 0; i < completedArr.size(); i++) {
                if (completedArr.getLong(i).equals(chapterId)) {
                    alreadyCompleted = true;
                    break;
                }
            }
            if (!alreadyCompleted) {
                completedArr.add(chapterId);
            }

            String newCompletedStr = completedArr.toJSONString();

            // 执行 UPSERT（覆盖 last_chapter_id + completed_chapters + last_learn_time）
            courseProgressMapper.upsertProgress(userId, courseId, chapterId, newCompletedStr);

            // 若本次是新完成（之前未完成），进行完课检测
            if (!alreadyCompleted) {
                checkCourseCompletion(userId, courseId, completedArr.size());
            }

        } catch (Exception e) {
            // 进度记录失败不影响章节内容返回，静默处理
            log.warn("[CourseService] 更新学习进度失败 userId={} courseId={} chapterId={}: {}",
                    userId, courseId, chapterId, e.getMessage());
        }
    }

    /**
     * 完课检测
     * 条件: completed_chapters.size() >= course.chapter_count
     * 排除: is_apod_course=1 或 is_mars_course=1（章节每天自动增加，永远无法完课）
     */
    private void checkCourseCompletion(Long userId, Long courseId, int completedCount) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) return;

        boolean isAutoSyncCourse = (course.getIsApodCourse() != null && course.getIsApodCourse() == 1)
                || (course.getIsMarsCourse() != null && course.getIsMarsCourse() == 1);
        if (isAutoSyncCourse) return;

        int totalChapters = course.getChapterCount() != null ? course.getChapterCount() : 0;
        if (totalChapters > 0 && completedCount >= totalChapters) {
            try {
                notificationHelper.sendCourseCompletedNotification(userId, courseId, course.getTitle());
            } catch (Exception e) {
                log.warn("[CourseService] 发送完课通知失败 userId={} courseId={}: {}", userId, courseId, e.getMessage());
            }
        }
    }

    // ============================== 收藏 ==============================

    @Override
    public boolean toggleFavorite(Long courseId, Long userId) {
        CourseFavorite existing = courseFavoriteMapper.selectByUserAndCourse(userId, courseId);
        if (existing != null) {
            courseFavoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            CourseFavorite favorite = new CourseFavorite();
            favorite.setUserId(userId);
            favorite.setCourseId(courseId);
            courseFavoriteMapper.insert(favorite);
            return true;
        }
    }

    /**
     * 我的课程收藏列表
     *
     * 📌 v5.3 修复：
     * - 新增 lastChapterId / lastChapterTitle / completedCount 字段填充
     *   （从 tb_course_progress 关联查询，供「继续学习/开始学习」判断）
     */
    @Override
    public IPage<CourseVO> getMyFavoriteList(Long userId, Integer pageNum, Integer pageSize) {
        Page<CourseFavorite> favPage = new Page<>(pageNum, pageSize);
        IPage<CourseFavorite> favList = courseFavoriteMapper.selectPage(favPage,
                new LambdaQueryWrapper<CourseFavorite>()
                        .eq(CourseFavorite::getUserId, userId)
                        .orderByDesc(CourseFavorite::getCreateTime)
        );

        Page<CourseVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(favList.getTotal());

        List<CourseVO> vos = favList.getRecords().stream().map(fav -> {
            Course course = courseMapper.selectById(fav.getCourseId());
            if (course == null || course.getDeleted() == 1) return null;

            CourseVO vo = buildCourseVO(course);
            vo.setIsFavorite(true);

            // ✅ v5.3 新增：查学习进度，填充 lastChapterId / lastChapterTitle / completedCount
            CourseProgress progress = courseProgressMapper.selectOne(
                    new LambdaQueryWrapper<CourseProgress>()
                            .eq(CourseProgress::getUserId, userId)
                            .eq(CourseProgress::getCourseId, fav.getCourseId())
            );
            if (progress != null) {
                vo.setLastChapterId(progress.getLastChapterId());
                // 查章节标题
                if (progress.getLastChapterId() != null) {
                    CourseChapter lastChapter = courseChapterMapper.selectById(progress.getLastChapterId());
                    if (lastChapter != null) {
                        vo.setLastChapterTitle(lastChapter.getTitle());
                    }
                }
                // 已完成章节数
                vo.setCompletedCount(parseCompletedCount(progress.getCompletedChapters()));
            } else {
                vo.setCompletedCount(0);
            }

            return vo;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        resultPage.setRecords(vos);
        return resultPage;
    }

    /**
     * 我的学习历史
     *
     * 📌 v5.3 修复：
     * - 新增 lastChapterTitle（JOIN chapter表查标题）
     * - 新增 completedCount（解析 completed_chapters JSON数组长度）
     * - lastChapterId=null 的记录同样返回（对应「浏览过但未学习」状态）
     *   CourseHistory.vue 前端对 null 显示「未学习」badge
     */
    @Override
    public IPage<CourseVO> getMyHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<CourseProgress> progressPage = new Page<>(pageNum, pageSize);
        IPage<CourseProgress> progressList = courseProgressMapper.selectPage(progressPage,
                new LambdaQueryWrapper<CourseProgress>()
                        .eq(CourseProgress::getUserId, userId)
                        .isNotNull(CourseProgress::getLastLearnTime)
                        .orderByDesc(CourseProgress::getLastLearnTime)
        );

        Page<CourseVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(progressList.getTotal());

        List<CourseVO> vos = progressList.getRecords().stream().map(progress -> {
            Course course = courseMapper.selectById(progress.getCourseId());
            if (course == null || course.getDeleted() == 1) return null;

            CourseVO vo = buildCourseVO(course);

            // ✅ v5.3 修复：填充完整进度信息
            vo.setLastChapterId(progress.getLastChapterId());

            // 查上次章节标题
            if (progress.getLastChapterId() != null) {
                CourseChapter lastChapter = courseChapterMapper.selectById(progress.getLastChapterId());
                if (lastChapter != null) {
                    vo.setLastChapterTitle(lastChapter.getTitle());
                }
            }
            // lastChapterId=null → lastChapterTitle=null → 前端显示「未学习」

            // 已完成章节数
            vo.setCompletedCount(parseCompletedCount(progress.getCompletedChapters()));

            // 收藏状态
            CourseFavorite fav = courseFavoriteMapper.selectByUserAndCourse(userId, course.getId());
            vo.setIsFavorite(fav != null);

            return vo;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        resultPage.setRecords(vos);
        return resultPage;
    }

    // ============================== 5.4 推荐课程 ==============================

    /**
     * 根据用户近3个月购买商品的标签推荐相关课程
     *
     * 完整推荐流程（7步兜底保障任意情况下都有合理结果）:
     *
     * Step 1: userId=null（未登录）
     *         → 直接返回热门6个（不暴露任何用户信息）
     *
     * Step 2: 查用户近3个月非取消订单(status!=4)中商品的 tags JSON 字符串列表
     *         → 每条结果是一个商品的完整 tags JSON，如 '["深空摄影","望远镜使用"]'
     *
     * Step 3: 查询结果为空（近3个月无购买记录）
     *         → 热门兜底，前端仍会显示推荐区块（展示热门），提升转化
     *
     * Step 4: 逐条解析 tags JSON 字符串 → 合并到 Set 去重
     *         → 得到如 {"深空摄影", "望远镜使用", "赤道仪"} 的标签集合
     *
     * Step 5: 合并后标签集仍为空（tags字段全部解析失败/格式异常）
     *         → 热门兜底
     *
     * Step 6: LIKE 匹配 tb_course.tags（OR关系，命中任意tag即入选）
     *         → 同时排除已有 tb_course_progress 记录的课程
     *         → 按 view_count 倒序，最多取6个
     *
     * Step 7: 匹配结果为空（没有课程含有用户购买商品的标签）
     *         → 热门兜底
     *
     * @param userId 当前用户ID（null=未登录）
     * @return 推荐课程列表，最多6个
     */
    @Override
    public List<CourseVO> getRecommendCourses(Long userId) {
        final int MAX_COUNT = 6;

        // Step 1: 未登录 → 直接热门兜底
        if (userId == null) {
            log.debug("[CourseRecommend] 未登录用户，返回热门兜底");
            return courseMapper.getHotCourses(null, MAX_COUNT);
        }

        // Step 2: 查近3个月已完成订单的商品 tags（排除已取消 status=4）
        LocalDateTime since = LocalDateTime.now().minusMonths(3);
        List<String> productTagsJsonList = courseMapper.getUserRecentOrderProductTags(userId, since);

        // Step 3: 无购买记录 → 热门兜底
        if (productTagsJsonList == null || productTagsJsonList.isEmpty()) {
            log.info("[CourseRecommend] userId={} 近3个月无购买记录，走热门兜底", userId);
            return courseMapper.getHotCourses(userId, MAX_COUNT);
        }

        // Step 4: 解析所有商品 tags JSON → 合并去重到 Set
        Set<String> userTagSet = new LinkedHashSet<>();  // 保持插入顺序（便于日志观察）
        for (String tagsJson : productTagsJsonList) {
            if (!StringUtils.hasText(tagsJson)) continue;
            try {
                List<String> tags = JSON.parseArray(tagsJson, String.class);
                if (tags != null) {
                    userTagSet.addAll(tags);
                }
            } catch (Exception e) {
                // tags JSON 格式异常时跳过该条，不影响整体推荐
                log.warn("[CourseRecommend] tags JSON 解析失败，已跳过: {}", tagsJson);
            }
        }

        // Step 5: 合并后无有效标签 → 热门兜底
        if (userTagSet.isEmpty()) {
            log.info("[CourseRecommend] userId={} 商品tags均为空或解析失败，走热门兜底", userId);
            return courseMapper.getHotCourses(userId, MAX_COUNT);
        }

        log.info("[CourseRecommend] userId={} 从购买商品提取到标签: {}", userId, userTagSet);

        // Step 6: LIKE 匹配课程（排除已学习，按 view_count 倒序）
        List<String> tagList = new ArrayList<>(userTagSet);
        List<CourseVO> recommended = courseMapper.getRecommendByTags(userId, tagList, MAX_COUNT);

        // Step 7: 无命中 → 热门兜底
        if (recommended == null || recommended.isEmpty()) {
            log.info("[CourseRecommend] userId={} 标签 {} 无匹配课程，走热门兜底", userId, userTagSet);
            return courseMapper.getHotCourses(userId, MAX_COUNT);
        }

        log.info("[CourseRecommend] userId={} 标签匹配到 {} 门课程", userId, recommended.size());
        return recommended;
    }

    // ============================== 私有工具方法 ==============================

    /** Course 实体 → CourseVO（基础字段映射） */
    private CourseVO buildCourseVO(Course course) {
        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setTitle(course.getTitle());
        vo.setSubtitle(course.getSubtitle());
        vo.setCover(course.getCover());
        vo.setType(course.getType());
        vo.setTypeText(course.getType() == 0 ? "视频课" : "书本课");
        vo.setDifficulty(course.getDifficulty());
        vo.setDifficultyText(getDifficultyText(course.getDifficulty()));
        vo.setTags(course.getTags());
        vo.setChapterCount(course.getChapterCount());
        vo.setViewCount(course.getViewCount());
        vo.setIsApodCourse(course.getIsApodCourse());
        vo.setIsMarsCourse(course.getIsMarsCourse());
        vo.setIsFavorite(false);
        vo.setCompletedCount(0);
        return vo;
    }

    /** CourseChapter 实体 → CourseChapterVO（含正文字段） */
    private CourseChapterVO buildChapterVO(CourseChapter chapter) {
        CourseChapterVO vo = new CourseChapterVO();
        vo.setId(chapter.getId());
        vo.setCourseId(chapter.getCourseId());
        vo.setTitle(chapter.getTitle());
        vo.setType(chapter.getType());
        vo.setSort(chapter.getSort());
        vo.setDuration(chapter.getDuration());
        vo.setVideoUrl(chapter.getVideoUrl());
        vo.setContent(chapter.getContent());
        vo.setSource(chapter.getSource());
        vo.setApodImage(chapter.getApodImage());
        return vo;
    }

    /**
     * 解析 completed_chapters JSON 字符串，返回已完成章节数
     * 兼容 null / 空字符串 / 格式错误情况
     */
    private int parseCompletedCount(String completedChapters) {
        if (!StringUtils.hasText(completedChapters)) return 0;
        try {
            return JSON.parseArray(completedChapters).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getDifficultyText(Integer difficulty) {
        if (difficulty == null) return "未知";
        switch (difficulty) {
            case 1: return "入门";
            case 2: return "进阶";
            case 3: return "高级";
            default: return "未知";
        }
    }
}
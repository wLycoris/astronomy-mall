package com.astronomy.mall.module.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.ApodSyncDTO;
import com.astronomy.mall.module.admin.dto.ChapterCreateDTO;
import com.astronomy.mall.module.admin.dto.CourseCreateDTO;
import com.astronomy.mall.module.admin.service.AdminCourseService;
import com.astronomy.mall.module.admin.vo.AdminCourseVO;
import com.astronomy.mall.module.course.entity.Course;
import com.astronomy.mall.module.course.entity.CourseChapter;
import com.astronomy.mall.module.course.mapper.CourseChapterMapper;
import com.astronomy.mall.module.course.mapper.CourseFavoriteMapper;
import com.astronomy.mall.module.course.mapper.CourseMapper;
import com.astronomy.mall.module.nasa.service.NasaApiService;
import com.astronomy.mall.module.nasa.vo.ApodVO;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台课程管理 Service 实现
 *
 * 📌 APOD 批量同步事务设计说明（重要）：
 *   syncApodRange()      → 无事务（NOT_SUPPORTED），只做循环调度
 *   insertOneApodDay()   → REQUIRES_NEW，每天独立提交
 *
 *   原因：如果用一个大 @Transactional 包住整个循环，
 *   NASA API 对某些历史日期返回 500 时会让 JDBC 连接进入
 *   rollback-only 状态，导致后续所有 INSERT 全部失败，最终新增 0 条。
 *   改为每天独立事务后，某天 NASA 返回 500 只跳过该天，其余天正常提交。
 *
 * 📌 self 字段（@Lazy 自注入）：
 *   用于在同类中调用 insertOneApodDay()，保证 @Transactional(REQUIRES_NEW) 生效。
 *   直接 this.insertOneApodDay() 会绕过 Spring AOP 代理，事务不生效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseFavoriteMapper courseFavoriteMapper;
    private final NasaApiService nasaApiService;
    private final NotificationHelper notificationHelper;

    /**
     * 自注入：让 insertOneApodDay() 的 @Transactional(REQUIRES_NEW) 通过 AOP 代理生效
     * @Lazy 避免循环依赖报错
     */
    @Autowired
    @Lazy
    private AdminCourseService self;

    // ==========================================
    // ============= 课程 CRUD ==================
    // ==========================================

    @Override
    public Page<AdminCourseVO> getCourseList(Integer pageNum, Integer pageSize,
                                             String keyword, Integer type, Integer status) {
        Page<Course> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getDeleted, 0)
                .like(StringUtils.hasText(keyword), Course::getTitle, keyword)
                .eq(type != null && type >= 0, Course::getType, type)
                .eq(status != null && status >= 0, Course::getStatus, status)
                .orderByDesc(Course::getSort)
                .orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(page, wrapper);

        Page<AdminCourseVO> voPage = new Page<>(
                coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        voPage.setRecords(coursePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCourse(CourseCreateDTO dto) {
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setSubtitle(dto.getSubtitle());
        course.setCover(dto.getCover());
        course.setType(dto.getType());
        course.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1);
        course.setSort(dto.getSort() != null ? dto.getSort() : 0);
        course.setStatus(0);
        course.setDeleted(0);
        course.setChapterCount(0);
        course.setViewCount(0);
        course.setIsApodCourse(0);
        course.setIsMarsCourse(0);
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            course.setTags(JSON.toJSONString(dto.getTags()));
        }
        courseMapper.insert(course);
        log.info("[AdminCourse] 新增课程: id={}, title={}", course.getId(), course.getTitle());
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long id, CourseCreateDTO dto) {
        Course course = getCourseOrThrow(id);
        if (StringUtils.hasText(dto.getTitle()))    course.setTitle(dto.getTitle());
        if (StringUtils.hasText(dto.getSubtitle())) course.setSubtitle(dto.getSubtitle());
        if (StringUtils.hasText(dto.getCover()))    course.setCover(dto.getCover());
        if (dto.getType() != null)                  course.setType(dto.getType());
        if (dto.getDifficulty() != null)            course.setDifficulty(dto.getDifficulty());
        if (dto.getSort() != null)                  course.setSort(dto.getSort());
        // tags 传空数组 [] → 清空；传 null → 不修改
        if (dto.getTags() != null) {
            course.setTags(dto.getTags().isEmpty() ? null : JSON.toJSONString(dto.getTags()));
        }
        courseMapper.updateById(course);
        log.info("[AdminCourse] 编辑课程: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        getCourseOrThrow(id);  // 先校验是否存在
        // 用 LambdaUpdateWrapper 直接 SET deleted=1，
        // 绕过 @TableLogic 对 updateById 的字段保护，确保软删除真正写库
        int rows = courseMapper.update(null, new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, id)
                .set(Course::getDeleted, 1));
        if (rows == 0) throw new BusinessException("删除失败，请稍后重试");
        log.info("[AdminCourse] 逻辑删除课程: id={}, 影响行数={}", id, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效：0=下架 1=发布");
        }
        getCourseOrThrow(id);
        courseMapper.update(null, new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, id)
                .set(Course::getStatus, status));
        log.info("[AdminCourse] 课程 {} 状态 → {}", id, status == 1 ? "已发布" : "草稿");
    }

    // ==========================================
    // ============= 章节管理 ===================
    // ==========================================

    @Override
    public List<AdminCourseVO.ChapterVO> getChapterList(Long courseId) {
        List<CourseChapter> list = courseChapterMapper.selectList(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
                        .orderByAsc(CourseChapter::getSort));
        return list.stream().map(this::convertChapterToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addChapter(ChapterCreateDTO dto) {
        Course course = getCourseOrThrow(dto.getCourseId());

        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(dto.getCourseId());
        chapter.setTitle(dto.getTitle());
        chapter.setType(dto.getType());
        chapter.setVideoUrl(dto.getVideoUrl());
        chapter.setContent(dto.getContent());
        chapter.setDuration(dto.getDuration() != null ? dto.getDuration() : 0);
        chapter.setSource("manual");

        if (dto.getSort() != null) {
            chapter.setSort(dto.getSort());
        } else {
            Integer maxSort = courseChapterMapper.getMaxSort(dto.getCourseId());
            chapter.setSort(maxSort == null ? 0 : maxSort + 1);
        }

        courseChapterMapper.insert(chapter);
        courseMapper.incrChapterCount(dto.getCourseId());
        log.info("[AdminCourse] 新增章节: chapterId={}, courseId={}", chapter.getId(), dto.getCourseId());

        // 🔔 通知：查收藏用户 → 传给 Helper 异步发送
        List<Long> favoriteUserIds =
                courseFavoriteMapper.selectUserIdsByCourseId(dto.getCourseId());
        notificationHelper.sendCourseChapterAddedNotification(
                dto.getCourseId(), course.getTitle(), dto.getTitle(), favoriteUserIds);

        return chapter.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(Long id, ChapterCreateDTO dto) {
        CourseChapter chapter = getChapterOrThrow(id);
        if (StringUtils.hasText(dto.getTitle())) chapter.setTitle(dto.getTitle());
        if (dto.getType() != null)               chapter.setType(dto.getType());
        if (dto.getVideoUrl() != null)           chapter.setVideoUrl(dto.getVideoUrl());
        if (dto.getContent() != null)            chapter.setContent(dto.getContent());
        if (dto.getDuration() != null)           chapter.setDuration(dto.getDuration());
        if (dto.getSort() != null)               chapter.setSort(dto.getSort());
        courseChapterMapper.updateById(chapter);
        log.info("[AdminCourse] 编辑章节: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long id) {
        CourseChapter chapter = getChapterOrThrow(id);
        Long courseId = chapter.getCourseId();
        courseChapterMapper.deleteById(id);
        courseMapper.update(null, new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, courseId)
                .setSql("chapter_count = GREATEST(chapter_count - 1, 0)"));
        log.info("[AdminCourse] 删除章节: id={}, courseId={}", id, courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortChapters(List<Map<String, Object>> sortList) {
        if (sortList == null || sortList.isEmpty()) return;
        for (Map<String, Object> item : sortList) {
            Long id      = Long.valueOf(item.get("id").toString());
            Integer sort = Integer.valueOf(item.get("sort").toString());
            courseChapterMapper.update(null, new LambdaUpdateWrapper<CourseChapter>()
                    .eq(CourseChapter::getId, id)
                    .set(CourseChapter::getSort, sort));
        }
        log.info("[AdminCourse] 批量更新章节排序，共 {} 条", sortList.size());
    }

    // ==========================================
    // ============= APOD 批量同步 ==============
    // ==========================================

    /**
     * 手动批量同步 APOD 历史数据（入口方法）
     *
     * ⚠️ 故意不加 @Transactional（Propagation.NOT_SUPPORTED）：
     *   每天的 INSERT 由 insertOneApodDay() 独立提交（REQUIRES_NEW）。
     *   这样 NASA API 500 导致某天跳过时，其他天的提交不受影响。
     *   如果整个方法包在一个大事务里，任何一次 NASA 500 都会让
     *   JDBC 连接进入 rollback-only，最终所有天的数据全部丢失。
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int syncApodRange(ApodSyncDTO dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate   = dto.getEndDate();

        // 参数校验（在事务外做，失败直接抛 BusinessException 返回前端）
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (endDate.isAfter(LocalDate.now())) {
            throw new BusinessException("结束日期不能晚于今天");
        }
        long days = endDate.toEpochDay() - startDate.toEpochDay();
        if (days > 60) {
            throw new BusinessException("单次同步范围不能超过60天，请分批操作");
        }

        // 找 APOD 专属课程（不在事务内，只是读操作）
        Course apodCourse = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getIsApodCourse, 1)
                .eq(Course::getDeleted, 0)
                .last("LIMIT 1"));
        if (apodCourse == null) {
            throw new BusinessException("未找到 APOD 专属课程（is_apod_course=1），请先创建");
        }

        // 查已存在日期（去重集合，避免每天都查一次数据库）
        List<LocalDate> existingDates = courseChapterMapper.selectList(
                        new LambdaQueryWrapper<CourseChapter>()
                                .eq(CourseChapter::getCourseId, apodCourse.getId())
                                .eq(CourseChapter::getSource, "apod")
                                .select(CourseChapter::getApodDate))
                .stream()
                .filter(c -> c.getApodDate() != null)
                .map(CourseChapter::getApodDate)
                .collect(Collectors.toList());

        int successCount = 0;
        int skipCount    = 0;
        int failCount    = 0;

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            final LocalDate cur = cursor;

            // 已存在：跳过
            if (existingDates.contains(cur)) {
                skipCount++;
                cursor = cursor.plusDays(1);
                continue;
            }

            try {
                // 先拉 NASA 数据（在事务外，网络异常不影响 JDBC 状态）
                ApodVO apod = nasaApiService.getApodByDate(cur);
                if (apod == null) {
                    log.warn("[ApodSync] NASA 返回 null，跳过: {}", cur);
                    failCount++;
                    cursor = cursor.plusDays(1);
                    continue;
                }

                // 通过 self（AOP 代理）调用，保证 REQUIRES_NEW 事务生效
                // ⚠️ 直接 this.insertOneApodDay() 会绕过代理，事务不生效
                self.insertOneApodDay(apodCourse.getId(), cur, apod);
                successCount++;
                log.info("[ApodSync] 同步成功: date={}, title={}", cur, apod.getTitle());

            } catch (Exception e) {
                // 单天失败：记录日志，继续下一天（不影响已提交的其他天）
                failCount++;
                log.warn("[ApodSync] 日期 {} 同步失败，跳过: {}", cur, e.getMessage());
            }

            cursor = cursor.plusDays(1);
        }

        log.info("[ApodSync] 批量同步完成: {} ~ {} | 新增={} 跳过={} 失败={}",
                startDate, endDate, successCount, skipCount, failCount);
        return successCount;
    }

    /**
     * 单天 APOD 数据入库（独立事务）
     *
     * Propagation.REQUIRES_NEW：每次调用开启新事务，立即提交或回滚，
     * 与外层 syncApodRange() 的"无事务"状态完全隔离。
     *
     * 📌 此方法必须通过 Spring AOP 代理调用（即通过 self.insertOneApodDay(...)），
     *    不能用 this.insertOneApodDay(...)，否则 @Transactional 不生效。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void insertOneApodDay(Long apodCourseId, LocalDate date, ApodVO apod) {
        Integer maxSort = courseChapterMapper.getMaxSort(apodCourseId);

        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(apodCourseId);
        chapter.setTitle(apod.getTitle() != null ? apod.getTitle() : "APOD " + date);
        chapter.setType(1);           // 书本课
        chapter.setSource("apod");
        chapter.setApodDate(date);
        chapter.setApodImage("image".equals(apod.getMediaType()) ? apod.getUrl() : null);
        chapter.setDuration(5);
        chapter.setSort(maxSort == null ? 0 : maxSort + 1);

        // 富文本内容：标题 + 配图/视频 + NASA 说明
        String mediaHtml = "image".equals(apod.getMediaType())
                ? String.format("<p><img src=\"%s\" alt=\"%s\" style=\"max-width:100%%;\" /></p>",
                apod.getHdurl() != null ? apod.getHdurl() : apod.getUrl(), apod.getTitle())
                : String.format("<p><iframe src=\"%s\" frameborder=\"0\" allowfullscreen "
                + "style=\"width:100%%;aspect-ratio:16/9;\"></iframe></p>", apod.getUrl());
        chapter.setContent(String.format("<h2>%s</h2>%s<p>%s</p>",
                apod.getTitle(), mediaHtml,
                apod.getExplanation() != null ? apod.getExplanation() : ""));

        courseChapterMapper.insert(chapter);
        courseMapper.incrChapterCount(apodCourseId);
    }

    // ==========================================
    // =========== 私有工具方法 =================
    // ==========================================

    private Course getCourseOrThrow(Long id) {
        Course c = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getId, id).eq(Course::getDeleted, 0));
        if (c == null) throw new BusinessException("课程不存在或已删除");
        return c;
    }

    private CourseChapter getChapterOrThrow(Long id) {
        CourseChapter c = courseChapterMapper.selectById(id);
        if (c == null) throw new BusinessException("章节不存在");
        return c;
    }

    private AdminCourseVO convertToVO(Course c) {
        AdminCourseVO vo = new AdminCourseVO();
        vo.setId(c.getId());
        vo.setTitle(c.getTitle());
        vo.setSubtitle(c.getSubtitle());
        vo.setCover(c.getCover());
        vo.setType(c.getType());
        vo.setTypeText(c.getType() == 0 ? "视频课" : "书本课");
        vo.setDifficulty(c.getDifficulty());
        vo.setDifficultyText(getDifficultyText(c.getDifficulty()));
        vo.setTags(c.getTags());
        vo.setChapterCount(c.getChapterCount());
        vo.setViewCount(c.getViewCount());
        vo.setIsApodCourse(c.getIsApodCourse());
        vo.setIsMarsCourse(c.getIsMarsCourse());
        vo.setStatus(c.getStatus());
        vo.setStatusText(c.getStatus() == 1 ? "已发布" : "草稿");
        vo.setSort(c.getSort());
        vo.setCreateTime(c.getCreateTime());
        vo.setUpdateTime(c.getUpdateTime());
        return vo;
    }

    private AdminCourseVO.ChapterVO convertChapterToVO(CourseChapter c) {
        AdminCourseVO.ChapterVO vo = new AdminCourseVO.ChapterVO();
        vo.setId(c.getId());
        vo.setTitle(c.getTitle());
        vo.setType(c.getType());
        vo.setVideoUrl(c.getVideoUrl());
        vo.setContent(c.getContent());
        vo.setSource(c.getSource());
        vo.setApodDate(c.getApodDate() != null ? c.getApodDate().toString() : null);
        vo.setApodImage(c.getApodImage());
        vo.setDuration(c.getDuration());
        vo.setSort(c.getSort());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }

    private String getDifficultyText(Integer d) {
        if (d == null) return "入门";
        switch (d) { case 2: return "进阶"; case 3: return "高级"; default: return "入门"; }
    }
}
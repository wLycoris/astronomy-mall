package com.astronomy.mall.module.course.task;

import com.astronomy.mall.module.course.entity.Course;
import com.astronomy.mall.module.course.entity.CourseFavorite;
import com.astronomy.mall.module.course.entity.CourseChapter;
import com.astronomy.mall.module.course.mapper.CourseFavoriteMapper;
import com.astronomy.mall.module.course.mapper.CourseChapterMapper;
import com.astronomy.mall.module.course.mapper.CourseMapper;
import com.astronomy.mall.module.nasa.service.NasaApiService;
import com.astronomy.mall.module.nasa.vo.ApodVO;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * APOD 定时同步调度器
 *
 * 📌 功能说明:
 * 1. 每天凌晨2点自动同步当日 NASA APOD 数据到「NASA每日天文图片精选」书本课
 * 2. 提供 syncApodRange() 供管理员手动批量补录历史数据
 *    （AdminCourseController POST /api/admin/course/apod/sync 调用）
 *
 * 📌 去重机制:
 * - tb_course_chapter.apod_date + source='apod' 组合去重
 *
 * 📌 NasaApiService 统一入口说明:
 * - 定时任务：getTodayApod()，带当日内存缓存，每天只请求 NASA 一次
 * - 批量补录：getApodByDate(date)，不走缓存，直接请求
 * - ⚠️ 严禁直接使用 RestTemplate
 *
 * 📌 通知集成（NotificationHelper 真实签名）:
 * notificationHelper.sendCourseApodUpdatedNotification(apodCourseId, apodTitle, favoriteUserIds)
 * - 需在调用前查询 CourseFavoriteMapper 获取 favoriteUserIds
 * - @Async 在 NotificationHelper 内执行，不阻塞当前事务
 */
@Slf4j
@Component
public class APODSyncScheduler {

    @Autowired
    private NasaApiService nasaApiService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseChapterMapper courseChapterMapper;

    /** 查询课程收藏用户，用于通知推送 */
    @Autowired
    private CourseFavoriteMapper courseFavoriteMapper;

    @Autowired
    private NotificationHelper notificationHelper;

    // ============================================================
    // 1. 每日定时同步（凌晨 2:00）
    // ============================================================

    /**
     * 每天凌晨2点自动同步当日 APOD 为书本课新章节
     *
     * 执行流程:
     * 1. 查询 is_apod_course=1 的专属课程
     * 2. 调用 NasaApiService.getTodayApod()（含当日缓存）
     * 3. apod_date 去重检查
     * 4. INSERT tb_course_chapter（source='apod'）
     * 5. UPDATE chapter_count +1
     * 6. 查询收藏用户列表 → 异步发通知
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncTodayApod() {
        log.info("========== APOD 定时同步任务开始 ==========");
        try {
            // 1. 查询 APOD 专属课程
            Course apodCourse = getApodCourse();
            if (apodCourse == null) {
                log.warn("未找到 is_apod_course=1 的课程，请先执行种子数据 SQL，跳过本次同步");
                return;
            }

            // 2. 获取今日 APOD（带缓存）
            ApodVO apodData = nasaApiService.getTodayApod();
            if (apodData == null || apodData.getDate() == null || apodData.getTitle() == null) {
                log.warn("NASA APOD 数据获取失败或字段不完整，跳过今日同步");
                return;
            }

            // 3. 解析日期，去重检查
            LocalDate apodDate = LocalDate.parse(
                    apodData.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );
            if (isApodDateExists(apodCourse.getId(), apodDate)) {
                log.info("APOD 日期 [{}] 已存在，跳过重复同步", apodDate);
                return;
            }

            // 4. 构建并插入新章节
            CourseChapter chapter = buildApodChapter(apodCourse.getId(), apodData, apodDate);
            courseChapterMapper.insert(chapter);

            // 5. 章节数 +1
            courseMapper.incrChapterCount(apodCourse.getId());

            log.info("✅ APOD 同步成功: courseId={}, date={}, title={}",
                    apodCourse.getId(), apodDate, apodData.getTitle());

            // 6. 查询收藏该课程的用户，异步批量发通知
            //    NotificationHelper.sendCourseApodUpdatedNotification 签名:
            //    (Long apodCourseId, String apodTitle, List<Long> favoriteUserIds)
            List<Long> favoriteUserIds = getFavoritedUserIds(apodCourse.getId());
            notificationHelper.sendCourseApodUpdatedNotification(
                    apodCourse.getId(),
                    apodData.getTitle(),
                    favoriteUserIds
            );

        } catch (Exception e) {
            log.error("❌ APOD 定时同步任务异常", e);
            throw new RuntimeException("APOD 同步失败", e);
        }
        log.info("========== APOD 定时同步任务结束 ==========");
    }

    // ============================================================
    // 2. 管理员手动批量同步（由 AdminCourseServiceImpl 调用）
    // ============================================================

    /**
     * 管理员手动批量同步历史 APOD 数据
     *
     * 对应接口: POST /api/admin/course/apod/sync
     *
     * 📌 批量同步不发通知，避免轰炸用户（仅补录数据）
     *
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 实际新增的章节数量
     */
    public int syncApodRange(LocalDate startDate, LocalDate endDate) {
        log.info("========== APOD 批量同步开始: {} ~ {} ==========", startDate, endDate);

        Course apodCourse = getApodCourse();
        if (apodCourse == null) {
            log.warn("未找到 is_apod_course=1 的课程，批量同步终止");
            return 0;
        }

        int successCount = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            final LocalDate date = current;
            try {
                // 已存在直接跳过
                if (isApodDateExists(apodCourse.getId(), date)) {
                    log.debug("APOD [{}] 已存在，跳过", date);
                    current = current.plusDays(1);
                    continue;
                }

                // 不走缓存，直接请求历史 APOD
                ApodVO apodData = nasaApiService.getApodByDate(date);
                if (apodData == null || apodData.getDate() == null || apodData.getTitle() == null) {
                    log.warn("日期 [{}] NASA APOD 数据获取失败，跳过", date);
                    current = current.plusDays(1);
                    continue;
                }

                // 每日独立事务插入
                doInsertApodChapter(apodCourse.getId(), apodData, date);
                successCount++;
                log.info("✅ 批量同步成功: date={}, title={}", date, apodData.getTitle());

                // 每次请求间隔 500ms，防触发 NASA API 频率限制
                Thread.sleep(500);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("批量同步被中断，已完成 {} 条", successCount);
                break;
            } catch (Exception e) {
                log.error("批量同步日期 [{}] 失败: {}", date, e.getMessage());
            }
            current = current.plusDays(1);
        }

        log.info("========== APOD 批量同步完成: 共新增 {} 条章节 ==========", successCount);
        return successCount;
    }

    // ============================================================
    // 3. 私有工具方法
    // ============================================================

    /** 查询 is_apod_course=1 的专属课程 */
    private Course getApodCourse() {
        return courseMapper.selectOne(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getIsApodCourse, 1)
                        .eq(Course::getDeleted, 0)
                        .last("LIMIT 1")
        );
    }

    /** 检查指定课程指定日期是否已有 APOD 章节 */
    private boolean isApodDateExists(Long courseId, LocalDate apodDate) {
        return courseChapterMapper.selectCount(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
                        .eq(CourseChapter::getApodDate, apodDate)
                        .eq(CourseChapter::getSource, "apod")
        ) > 0;
    }

    /**
     * 查询收藏指定课程的所有用户 ID 列表
     *
     * @param courseId 课程ID
     * @return 用户 ID 列表；无收藏时返回空列表（空列表时 NotificationHelper 直接 return，不遍历）
     */
    private List<Long> getFavoritedUserIds(Long courseId) {
        try {
            List<CourseFavorite> favorites = courseFavoriteMapper.selectList(
                    new LambdaQueryWrapper<CourseFavorite>()
                            .eq(CourseFavorite::getCourseId, courseId)
                            .select(CourseFavorite::getUserId)
            );
            if (favorites == null || favorites.isEmpty()) {
                return Collections.emptyList();
            }
            return favorites.stream()
                    .map(CourseFavorite::getUserId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询课程收藏用户失败: courseId={}", courseId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建 APOD 章节实体（未持久化）
     */
    private CourseChapter buildApodChapter(Long courseId, ApodVO apodData, LocalDate apodDate) {
        String imageUrl = resolveApodImageUrl(apodData);
        Integer maxSort = courseChapterMapper.getMaxSort(courseId);
        int newSort = (maxSort == null ? 0 : maxSort) + 1;
        String content = buildApodContent(apodData, imageUrl);

        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(courseId);
        chapter.setTitle(apodData.getTitle());
        chapter.setType(1);              // 1=图文
        chapter.setContent(content);
        chapter.setSource("apod");
        chapter.setApodDate(apodDate);
        chapter.setApodImage(imageUrl);
        chapter.setSort(newSort);
        return chapter;
    }

    /** image→优先hdurl；video→url */
    private String resolveApodImageUrl(ApodVO apodData) {
        if ("image".equals(apodData.getMediaType())) {
            return (apodData.getHdurl() != null && !apodData.getHdurl().isEmpty())
                    ? apodData.getHdurl() : apodData.getUrl();
        }
        return apodData.getUrl();
    }

    /** 构建章节富文本 HTML（媒体 + explanation + 版权） */
    private String buildApodContent(ApodVO apodData, String mediaUrl) {
        StringBuilder sb = new StringBuilder();
        if ("image".equals(apodData.getMediaType())) {
            sb.append("<p style=\"text-align:center;\">")
                    .append("<img src=\"").append(mediaUrl).append("\" ")
                    .append("alt=\"").append(escapeHtml(apodData.getTitle())).append("\" ")
                    .append("style=\"max-width:100%;border-radius:8px;\" />")
                    .append("</p>");
        } else {
            sb.append("<p style=\"text-align:center;\">")
                    .append("<iframe src=\"").append(mediaUrl).append("\" ")
                    .append("width=\"100%\" height=\"400\" frameborder=\"0\" allowfullscreen ")
                    .append("style=\"border-radius:8px;\"></iframe>")
                    .append("</p>");
        }
        if (apodData.getExplanation() != null && !apodData.getExplanation().isEmpty()) {
            sb.append("<p>").append(apodData.getExplanation()).append("</p>");
        }
        if (apodData.getCopyright() != null && !apodData.getCopyright().isEmpty()) {
            sb.append("<p style=\"color:#999;font-size:12px;\">© ")
                    .append(escapeHtml(apodData.getCopyright())).append("</p>");
        }
        return sb.toString();
    }

    /**
     * 独立事务插入章节（批量补录时每日独立提交，单日失败不影响其他日期）
     */
    @Transactional(rollbackFor = Exception.class)
    public void doInsertApodChapter(Long courseId, ApodVO apodData, LocalDate date) {
        CourseChapter chapter = buildApodChapter(courseId, apodData, date);
        courseChapterMapper.insert(chapter);
        courseMapper.incrChapterCount(courseId);
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
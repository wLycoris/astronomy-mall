package com.astronomy.mall.module.course.task;

import com.astronomy.mall.module.course.entity.Course;
import com.astronomy.mall.module.course.entity.CourseChapter;
import com.astronomy.mall.module.course.mapper.CourseChapterMapper;
import com.astronomy.mall.module.course.mapper.CourseMapper;
import com.astronomy.mall.module.nasa.service.NasaApiService;
import com.astronomy.mall.module.nasa.vo.MarsPhotoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 火星探测车照片定时同步调度器
 *
 * 📌 功能说明:
 * 1. 每天凌晨2点30分自动将 NASA Mars Rover 最新照片同步到「火星探测车日志」书本课
 * 2. 按 earthDate 分组，每个日期生成一个章节
 * 3. 以 apod_date（复用字段）+ source='mars_rover' 去重
 *
 * 📌 与 APODSyncScheduler 的核心差异:
 * - APOD：每天一张图 → 一条章节
 * - Mars：每天多张图（可能数十到数百张）→ 按 earthDate 分组 → 每组一条章节
 *
 * 📌 NasaApiService 调用方式:
 * - ⚠️ 使用 getAllLatestMarsPhotos()（全量版），而非 getLatestMarsPhotos()（仅3张）
 * - 全量版返回最多200张，确保分组后能覆盖多个日期
 * - ⚠️ 严禁在本类直接使用 RestTemplate
 *
 * 📌 通知说明:
 * - 火星课暂不发通知（用户体验：火星课每天内容较多，避免通知疲劳）
 * - 如需启用，可参考 APODSyncScheduler 的通知模式注入 CourseFavoriteMapper + NotificationHelper
 */
@Slf4j
@Component
public class MarsRoverSyncScheduler {

    /** source 字段标识，与 tb_course_chapter.source 字段值对应 */
    private static final String SOURCE_MARS_ROVER = "mars_rover";

    /** 每个章节展示的最大照片数量（防止 content 字段过大） */
    private static final int MAX_PHOTOS_PER_CHAPTER = 20;

    @Autowired
    private NasaApiService nasaApiService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseChapterMapper courseChapterMapper;

    // ============================================================
    // 1. 每日定时同步（凌晨 2:30）
    // ============================================================

    /**
     * 每天凌晨2点30分自动同步最新火星车照片
     *
     * 执行流程:
     * 1. 查询 is_mars_course=1 的专属课程
     * 2. 调用 NasaApiService.getAllLatestMarsPhotos()（全量，最多200张）
     * 3. 按 earthDate 分组（可能覆盖多天）
     * 4. 对每个日期检查去重（apod_date + source='mars_rover'）
     * 5. 新增章节：title="火星探测车日志·{date}"，content=HTML 图片展示
     * 6. chapter_count +1
     */
    @Scheduled(cron = "0 30 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncLatestMarsPhotos() {
        log.info("========== 火星探测车照片同步任务开始 ==========");
        try {
            // 1. 查询火星专属课程
            Course marsCourse = getMarsCourse();
            if (marsCourse == null) {
                log.warn("未找到 is_mars_course=1 的课程，请先执行种子数据 SQL，跳过本次同步");
                return;
            }

            // 2. 全量获取最新火星车照片（最多200张，按日期分组用）
            //    ⚠️ 必须用 getAllLatestMarsPhotos()，getLatestMarsPhotos() 只返回3张
            List<MarsPhotoVO> photos = nasaApiService.getAllLatestMarsPhotos();
            if (CollectionUtils.isEmpty(photos)) {
                log.warn("NASA Mars Rover 照片数据为空，跳过本次同步");
                return;
            }
            log.info("获取到 {} 张火星车照片，开始按日期分组处理", photos.size());

            // 3. 按 earthDate 分组（LinkedHashMap 保持响应中的日期顺序）
            Map<String, List<MarsPhotoVO>> photosByDate = groupPhotosByDate(photos);
            log.info("共 {} 个地球日期的照片需要处理", photosByDate.size());

            int syncCount = 0;
            for (Map.Entry<String, List<MarsPhotoVO>> entry : photosByDate.entrySet()) {
                String earthDateStr = entry.getKey();
                List<MarsPhotoVO> dayPhotos = entry.getValue();

                try {
                    // 4. 解析日期，去重检查
                    LocalDate earthDate = LocalDate.parse(
                            earthDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    );
                    if (isMarsDateExists(marsCourse.getId(), earthDate)) {
                        log.debug("火星车日期 [{}] 已存在，跳过", earthDate);
                        continue;
                    }

                    // 5. 构建并插入章节
                    CourseChapter chapter = buildMarsChapter(marsCourse.getId(), earthDate, dayPhotos);
                    courseChapterMapper.insert(chapter);

                    // 6. 章节数 +1
                    courseMapper.incrChapterCount(marsCourse.getId());

                    syncCount++;
                    log.info("✅ 火星车照片同步成功: date={}, photos={}", earthDate, dayPhotos.size());

                } catch (Exception e) {
                    log.error("火星车日期 [{}] 章节同步失败: {}", earthDateStr, e.getMessage());
                    // 单日失败不影响其他日期
                }
            }

            log.info("火星车照片同步完成，本次新增 {} 个章节", syncCount);

        } catch (Exception e) {
            log.error("❌ 火星探测车照片同步任务异常", e);
            throw new RuntimeException("火星车照片同步失败", e);
        }
        log.info("========== 火星探测车照片同步任务结束 ==========");
    }

    // ============================================================
    // 2. 私有工具方法
    // ============================================================

    /** 查询 is_mars_course=1 的专属课程 */
    private Course getMarsCourse() {
        return courseMapper.selectOne(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getIsMarsCourse, 1)
                        .eq(Course::getDeleted, 0)
                        .last("LIMIT 1")
        );
    }

    /** 检查指定日期的火星车章节是否已存在（去重） */
    private boolean isMarsDateExists(Long courseId, LocalDate earthDate) {
        return courseChapterMapper.selectCount(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
                        .eq(CourseChapter::getApodDate, earthDate)   // 复用 apod_date 字段存 earthDate
                        .eq(CourseChapter::getSource, SOURCE_MARS_ROVER)
        ) > 0;
    }

    /**
     * 将照片列表按 earthDate 分组，保持顺序
     *
     * @param photos 全量照片列表
     * @return Map<earthDate字符串, 当天照片列表>（LinkedHashMap 保持顺序）
     */
    private Map<String, List<MarsPhotoVO>> groupPhotosByDate(List<MarsPhotoVO> photos) {
        return photos.stream()
                .filter(p -> p.getEarthDate() != null && p.getImgSrc() != null)
                .collect(Collectors.groupingBy(
                        MarsPhotoVO::getEarthDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * 构建火星车照片章节实体（未持久化）
     *
     * 章节结构:
     * - title:     "火星探测车日志 · yyyy年MM月dd日"
     * - apod_date: earthDate（复用，去重键）
     * - apod_image: 第一张照片 URL（章节封面）
     * - content:   HTML 格式的照片网格（两列，最多 MAX_PHOTOS_PER_CHAPTER 张）
     * - source:    "mars_rover"
     */
    private CourseChapter buildMarsChapter(Long courseId, LocalDate earthDate,
                                           List<MarsPhotoVO> dayPhotos) {
        String title = "火星探测车日志 · "
                + earthDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));

        // 第一张照片作为章节封面（apod_image 字段复用）
        String firstImageUrl = dayPhotos.get(0).getImgSrc();

        // 展示前 MAX_PHOTOS_PER_CHAPTER 张，防 content 过大
        List<MarsPhotoVO> displayPhotos = dayPhotos.size() > MAX_PHOTOS_PER_CHAPTER
                ? dayPhotos.subList(0, MAX_PHOTOS_PER_CHAPTER)
                : dayPhotos;

        long cameraCount = dayPhotos.stream()
                .map(p -> p.getCameraFullName() != null ? p.getCameraFullName() : "Unknown")
                .distinct().count();

        String content = buildMarsHtmlContent(earthDate, dayPhotos.size(), cameraCount, displayPhotos);

        Integer maxSort = courseChapterMapper.getMaxSort(courseId);
        int newSort = (maxSort == null ? 0 : maxSort) + 1;

        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(courseId);
        chapter.setTitle(title);
        chapter.setType(1);                   // 1=图文
        chapter.setContent(content);
        chapter.setSource(SOURCE_MARS_ROVER);
        chapter.setApodDate(earthDate);       // 复用 apod_date 存 earthDate（去重键）
        chapter.setApodImage(firstImageUrl);  // 封面图
        chapter.setSort(newSort);
        return chapter;
    }

    /**
     * 生成火星车照片章节的 HTML 内容
     *
     * 布局：统计信息 → 两列照片网格 → 数量提示 → 数据来源声明
     */
    private String buildMarsHtmlContent(LocalDate earthDate, int totalPhotos,
                                        long cameraCount, List<MarsPhotoVO> displayPhotos) {
        StringBuilder sb = new StringBuilder();

        // 统计信息头部
        sb.append("<p style=\"color:#666;font-size:14px;margin-bottom:16px;\">")
                .append("📅 地球日期：").append(earthDate)
                .append("&nbsp;&nbsp;|&nbsp;&nbsp;")
                .append("📸 共 ").append(totalPhotos).append(" 张照片")
                .append("&nbsp;&nbsp;|&nbsp;&nbsp;")
                .append("🔭 ").append(cameraCount).append(" 个相机拍摄")
                .append("</p>");

        // 两列照片网格
        sb.append("<div style=\"display:flex;flex-wrap:wrap;gap:12px;\">");
        for (MarsPhotoVO photo : displayPhotos) {
            String cameraName = (photo.getCameraFullName() != null
                    && !photo.getCameraFullName().isEmpty())
                    ? photo.getCameraFullName() : "Unknown Camera";

            sb.append("<div style=\"flex:0 0 calc(50% - 6px);box-sizing:border-box;\">")
                    .append("<img src=\"").append(photo.getImgSrc()).append("\" ")
                    .append("alt=\"").append(escapeHtml(cameraName)).append("\" ")
                    .append("style=\"width:100%;height:200px;object-fit:cover;border-radius:6px;\" ")
                    .append("loading=\"lazy\" />")
                    .append("<p style=\"font-size:12px;color:#888;margin:4px 0 8px;\">")
                    .append("📷 ").append(escapeHtml(cameraName))
                    .append("</p>")
                    .append("</div>");
        }
        sb.append("</div>");

        // 截断提示
        if (totalPhotos > MAX_PHOTOS_PER_CHAPTER) {
            sb.append("<p style=\"color:#999;font-size:12px;margin-top:12px;text-align:center;\">")
                    .append("本章节展示了 ").append(MAX_PHOTOS_PER_CHAPTER)
                    .append(" 张（共 ").append(totalPhotos).append(" 张）火星表面照片")
                    .append("</p>");
        }

        // 数据来源声明
        sb.append("<p style=\"color:#bbb;font-size:11px;margin-top:16px;")
                .append("border-top:1px solid #f0f0f0;padding-top:8px;\">")
                .append("数据来源：NASA Mars Rover Photos API · Powered by Astronomy Mall")
                .append("</p>");

        return sb.toString();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程 Service 实现
 *
 * 📌 核心设计说明:
 * 1. 多标签AND筛选: Service层 split(",") 后存入 dto.tagList，Mapper XML用 foreach + JSON_CONTAINS
 * 2. 进度UPSERT: 使用 INSERT ... ON DUPLICATE KEY UPDATE，不需要先查后写（原子性）
 * 3. 完课检测: completed_chapters.size() >= chapter_count 且不是APOD/火星课，触发通知
 * 4. 收藏通知: 收藏列表在 NotificationHelper 中获取，此处只触发发送
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

        // 3. 查当前用户进度（获取 completedChapters 用于章节目录 isCompleted 标记）
        String completedChapters = null;
        if (userId != null) {
            CourseProgress progress = courseProgressMapper.selectOne(
                    new LambdaQueryWrapper<CourseProgress>()
                            .eq(CourseProgress::getUserId, userId)
                            .eq(CourseProgress::getCourseId, courseId)
            );
            if (progress != null) {
                vo.setLastChapterId(progress.getLastChapterId());
                completedChapters = progress.getCompletedChapters();
            }

            // 4. 查收藏状态
            CourseFavorite favorite = courseFavoriteMapper.selectByUserAndCourse(userId, courseId);
            vo.setIsFavorite(favorite != null);
        }

        // 5. 查章节目录（不含正文，含 isCompleted 标记）
        List<CourseChapterVO> chapters =
                courseChapterMapper.selectChapterOutlineList(courseId, completedChapters);
        vo.setChapters(chapters);

        // 6. 更新 view_count（异步或直接+1，此处简单直接更新）
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

            // 执行 UPSERT
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
     * 排除: is_apod_course=1 或 is_mars_course=1 的课程（章节每天增加，永远无法"完课"）
     *
     * @param userId          用户ID
     * @param courseId        课程ID
     * @param completedCount  当前已完成章节数
     */
    private void checkCourseCompletion(Long userId, Long courseId, int completedCount) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) return;

        // 排除自动同步类课程
        boolean isAutoSyncCourse = (course.getIsApodCourse() != null && course.getIsApodCourse() == 1)
                || (course.getIsMarsCourse() != null && course.getIsMarsCourse() == 1);
        if (isAutoSyncCourse) return;

        int totalChapters = course.getChapterCount() != null ? course.getChapterCount() : 0;
        if (totalChapters > 0 && completedCount >= totalChapters) {
            // 触发完课通知（NotificationHelper @Async 异步发送，不阻塞当前请求）
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
            // 已收藏 → 取消
            courseFavoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            // 未收藏 → 收藏
            CourseFavorite favorite = new CourseFavorite();
            favorite.setUserId(userId);
            favorite.setCourseId(courseId);
            courseFavoriteMapper.insert(favorite);
            return true;
        }
    }

    @Override
    public IPage<CourseVO> getMyFavoriteList(Long userId, Integer pageNum, Integer pageSize) {
        // 查收藏的课程ID列表，再查课程详情
        Page<CourseFavorite> favPage = new Page<>(pageNum, pageSize);
        IPage<CourseFavorite> favList = courseFavoriteMapper.selectPage(favPage,
                new LambdaQueryWrapper<CourseFavorite>()
                        .eq(CourseFavorite::getUserId, userId)
                        .orderByDesc(CourseFavorite::getCreateTime)
        );

        // 转换为 CourseVO
        Page<CourseVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(favList.getTotal());
        List<CourseVO> vos = favList.getRecords().stream().map(fav -> {
            Course course = courseMapper.selectById(fav.getCourseId());
            if (course == null || course.getDeleted() == 1) return null;
            CourseVO vo = buildCourseVO(course);
            vo.setIsFavorite(true);
            return vo;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        resultPage.setRecords(vos);
        return resultPage;
    }

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
            vo.setLastChapterId(progress.getLastChapterId());
            // 计算完成进度
            CourseFavorite fav = courseFavoriteMapper.selectByUserAndCourse(userId, course.getId());
            vo.setIsFavorite(fav != null);
            return vo;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        resultPage.setRecords(vos);
        return resultPage;
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
        // 默认未收藏/未登录
        vo.setIsFavorite(false);
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
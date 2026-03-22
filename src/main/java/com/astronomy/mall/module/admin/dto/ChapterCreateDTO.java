package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 后台新增/编辑章节 DTO
 *
 * 对应接口:
 *   POST /api/admin/course/chapter/add
 *   PUT  /api/admin/course/chapter/{id}
 *
 * 📌 视频章节 URL 说明：
 *   前端按平台模板拼接完整嵌入 URL 后提交，后端直接存入 tb_course_chapter.video_url：
 *     B站:     https://player.bilibili.com/player.html?bvid={BV号}&page=1&high_quality=1&danmaku=0
 *     YouTube: https://www.youtube.com/embed/{视频ID}
 *     抖音:    https://open.douyin.com/player/video?vid={视频ID}
 *   也支持直接粘贴完整 URL 兜底。
 *   数据库始终存完整嵌入 URL，前端 iframe src 直接使用，无需二次转换。
 *
 * 📌 书本章节 content：
 *   由前端 TinyMCE 富文本编辑器生成的 HTML 字符串
 */
@Data
public class ChapterCreateDTO {

    /**
     * 所属课程 ID（新增时必填）
     */
    private Long courseId;

    /**
     * 章节标题（必填）
     */
    @NotBlank(message = "章节标题不能为空")
    private String title;

    /**
     * 内容类型：0-视频  1-图文（必填，应与课程 type 保持一致）
     */
    @NotNull(message = "章节类型不能为空")
    private Integer type;

    /**
     * 视频嵌入 URL（type=0 时有值，已是完整 embed URL）
     */
    private String videoUrl;

    /**
     * 富文本内容（type=1 时有值，TinyMCE 生成的 HTML）
     */
    private String content;

    /**
     * 预计学习时长（分钟，不传默认 0）
     */
    private Integer duration;

    /**
     * 章节排序值（越小越靠前，不传则自动追加到末尾）
     */
    private Integer sort;
}
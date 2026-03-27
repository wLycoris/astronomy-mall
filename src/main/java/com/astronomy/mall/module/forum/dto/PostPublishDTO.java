package com.astronomy.mall.module.forum.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 发帖/编辑帖子 DTO
 *
 * 对应接口:
 *   POST /api/post/publish   发布帖子
 *   PUT  /api/post/{id}      编辑帖子
 *
 * 📌 字段说明:
 *   title: 帖子标题，必填，最长200字符
 *   content: 帖子正文，必填
 *   images: base64 JSON数组字符串，最多9张，Canvas压缩后单张约150-300KB
 *   tags: JSON数组字符串，如 ["深空摄影","望远镜"]
 *   recognitionId: AI识别结果分享时关联（ForumPublish预填，可选）
 */
@Data
public class PostPublishDTO {

    /** 帖子标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长200字符")
    private String title;

    /** 帖子正文 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 图片(base64 JSON数组，最多9张) */
    private String images;

    /** 帖子标签(JSON数组) */
    private String tags;

    /** AI识别结果关联ID（可选，分享识别结果时传入） */
    private Long recognitionId;
}

package com.astronomy.mall.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 后台新增/编辑课程 DTO
 *
 * 对应接口:
 *   POST /api/admin/course/add
 *   PUT  /api/admin/course/update/{id}
 *
 * 📌 tags 字段：
 *   前端传 List<String>，如 ["深空摄影","望远镜"]
 *   Service 层用 JSON.toJSONString(tags) 序列化后存入 tb_course.tags varchar(500)
 *   与 tb_product.tags / tb_post.tags 保持一致，用于推荐系统标签匹配
 */
@Data
public class CourseCreateDTO {

    /**
     * 课程标题（必填）
     */
    @NotBlank(message = "课程标题不能为空")
    private String title;

    /**
     * 副标题 / 简介
     */
    private String subtitle;

    /**
     * 封面图 URL
     */
    private String cover;

    /**
     * 课程类型：0-视频课  1-书本课（必填）
     */
    @NotNull(message = "课程类型不能为空")
    private Integer type;

    /**
     * 难度：1-入门  2-进阶  3-高级（不传默认 1）
     */
    private Integer difficulty;

    /**
     * 课程标签列表，如 ["深空摄影", "望远镜"]
     * Service 层序列化为 JSON 字符串存入数据库
     */
    private List<String> tags;

    /**
     * 排序值（越大越靠前，不传默认 0）
     */
    private Integer sort;
}
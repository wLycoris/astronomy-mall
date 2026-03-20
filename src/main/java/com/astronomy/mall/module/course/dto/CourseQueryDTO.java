package com.astronomy.mall.module.course.dto;

import lombok.Data;

import java.util.List;

/**
 * 课程列表查询参数 DTO
 * 对应接口: GET /api/course/list
 *
 * 📌 多标签AND筛选说明:
 * - tags 参数传逗号分隔的标签字符串，如 "深空摄影,望远镜使用"
 * - 后端对每个tag都做 JSON_CONTAINS 检测，取交集（AND关系）
 * - 多个tag同时选中时，返回同时包含所有选中tag的课程
 */
@Data
public class CourseQueryDTO {

    /** 页码（默认1） */
    private Integer pageNum = 1;

    /** 每页条数（默认12） */
    private Integer pageSize = 12;

    /**
     * 课程类型筛选
     * null-全部  0-视频课  1-书本课
     */
    private Integer type;

    /**
     * 难度筛选
     * null-全部  1-入门  2-进阶  3-高级
     */
    private Integer difficulty;

    /**
     * 关键词搜索（匹配课程标题）
     * 空时不筛选
     */
    private String keyword;

    /**
     * 标签筛选原始字符串（前端传入，逗号分隔）
     * 示例: "深空摄影,望远镜使用,月球"
     * Service层 split(",") 后写入 tagList，Mapper XML 使用 tagList
     */
    private String tags;

    /**
     * 标签列表（Service层处理后填充，Mapper XML 中 foreach 使用此字段）
     * ⚠️ 前端不传此字段，后端内部由 Service 解析 tags 后填充
     */
    private List<String> tagList;
}
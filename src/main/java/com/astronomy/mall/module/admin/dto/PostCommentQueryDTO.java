package com.astronomy.mall.module.admin.dto;

import lombok.Data;

/**
 * 后台评论列表查询 DTO（管理员用）
 *
 * 对应接口: GET /api/admin/post/comment/list
 *
 * 📌 7.7 后台管理 - 评论管理
 *   - postId:   按帖子ID筛选（null=全部帖子的评论）
 *   - keyword:  评论内容关键词
 *   - pageNum:  页码
 *   - pageSize: 每页条数
 */
@Data
public class PostCommentQueryDTO {

    /** 按帖子ID筛选（可选） */
    private Long postId;

    /** 评论内容关键词（可选） */
    private String keyword;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 20;
}

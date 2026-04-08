package com.astronomy.mall.module.admin.dto;

import lombok.Data;

/**
 * 后台帖子列表查询 DTO（管理员用）
 *
 * 对应接口: GET /api/admin/post/list
 *
 * 📌 7.7 后台管理 - 帖子列表筛选
 *   - status:   状态筛选 (0草稿 1审核中 2已发布 3已拒绝 4管理员删除)，null=全部
 *   - keyword:  关键词搜索（模糊匹配 title / content / 作者昵称）
 *   - pageNum:  页码，默认1
 *   - pageSize: 每页条数，默认20
 */
@Data
public class PostQueryDTO {

    /** 状态筛选 (0草稿/1审核中/2已发布/3已拒绝/4管理员删除)，null=全部 */
    private Integer status;

    /** 关键词（标题/内容/昵称模糊匹配） */
    private String keyword;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 20;
}

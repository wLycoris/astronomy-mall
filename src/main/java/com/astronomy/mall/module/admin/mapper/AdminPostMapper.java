package com.astronomy.mall.module.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 后台论坛帖子管理 Mapper（7.7）
 *
 * 文件位置: module/admin/mapper/AdminPostMapper.java
 * XML 位置: resources/mapper/AdminPostMapper.xml
 *
 * 📌 设计原则：
 *   - 所有"管理员视角"的查询都从 forum 模块的 PostMapper 拆出来放到这里
 *   - forum/PostMapper 专注于用户视角（已发布、可见、未删）
 *   - admin/AdminPostMapper 专注于管理员视角（含草稿/审核/拒绝/管理员删除等所有状态）
 *
 * 📌 不继承 BaseMapper<Post>：因为 Post 实体在 forum 模块下，
 *    管理员这里只做查询/统计，不做基础 CRUD（CRUD 仍走 forum.PostMapper）
 */
@Mapper
public interface AdminPostMapper {

    /**
     * 7.7: 后台帖子列表（管理员视角，可查所有状态）
     * - 不限制 status，可按 status 筛选
     * - 按 keyword 模糊匹配 title / content / 作者昵称
     * - 默认按 is_top DESC, create_time DESC（置顶优先 + 最新优先）
     */
    List<Map<String, Object>> selectAdminPostList(@Param("status") Integer status,
                                                  @Param("keyword") String keyword,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    /**
     * 7.7: 后台帖子列表总数
     */
    long countAdminPostList(@Param("status") Integer status,
                            @Param("keyword") String keyword);

    /**
     * 7.7: 待审核帖子数量（status=1，导航角标用）
     */
    long countPendingPosts();

    /**
     * 7.7: 论坛统计 - 帖子状态分布（饼图用）
     * 返回 [{status:1, count:10}, {status:2, count:120}, ...]
     */
    List<Map<String, Object>> selectStatusDistribution();

    /**
     * 7.7: 论坛统计 - 近N天每天发帖数量（折线图用）
     * 返回 [{date:"2026-04-01", count:10}, ...]
     */
    List<Map<String, Object>> selectDailyPostCount(@Param("days") int days);

    /**
     * 7.7: 论坛统计 - 总帖子数（不含管理员删除 status=4）
     */
    long countTotalPosts();

    /**
     * 7.7: 论坛统计 - 累计发帖用户数（去重）
     */
    long countTotalAuthors();

    /**
     * 7.7: 论坛统计 - 今日发帖数
     */
    long countTodayPosts();
}

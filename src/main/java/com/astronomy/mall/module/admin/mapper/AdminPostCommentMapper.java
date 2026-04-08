package com.astronomy.mall.module.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 后台论坛评论管理 Mapper（7.7）
 *
 * 文件位置: module/admin/mapper/AdminPostCommentMapper.java
 * XML 位置: resources/mapper/AdminPostCommentMapper.xml
 *
 * 📌 设计原则：
 *   - "管理员视角"的评论查询统统放这里，与 forum.PostCommentMapper 解耦
 *   - forum/PostCommentMapper 只负责用户视角（顶级/子评论、已显示）
 *   - admin/AdminPostCommentMapper 负责后台筛选/统计
 *
 * 📌 不继承 BaseMapper<PostComment>：因为 PostComment 实体在 forum 模块下，
 *    管理员这里只做查询/统计，CRUD（包括软删）仍走 forum.PostCommentMapper
 */
@Mapper
public interface AdminPostCommentMapper {

    /**
     * 7.7: 后台评论列表（管理员视角）
     * - 可按 postId 筛选某帖子下的评论
     * - 可按 keyword 模糊匹配 content
     * - JOIN tb_user 获取昵称头像，JOIN tb_post 获取帖子标题
     * - 按 create_time 倒序
     */
    List<Map<String, Object>> selectAdminCommentList(@Param("postId") Long postId,
                                                     @Param("keyword") String keyword,
                                                     @Param("offset") int offset,
                                                     @Param("pageSize") int pageSize);

    /**
     * 7.7: 后台评论列表总数
     */
    long countAdminCommentList(@Param("postId") Long postId,
                               @Param("keyword") String keyword);

    /**
     * 7.7: 论坛统计 - 总评论数
     */
    long countTotalComments();

    /**
     * 7.7: 论坛统计 - 今日评论数
     */
    long countTodayComments();

    /**
     * 7.7: 论坛统计 - 今日活跃用户数（发帖+评论 去重）
     */
    long countTodayActiveUsers();

    /**
     * 7.7: 论坛统计 - 近N天每天评论数量（折线图用）
     * 返回 [{date:"2026-04-01", count:25}, ...]
     */
    List<Map<String, Object>> selectDailyCommentCount(@Param("days") int days);
}

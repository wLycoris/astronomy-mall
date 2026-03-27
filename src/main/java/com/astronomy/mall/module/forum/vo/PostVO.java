package com.astronomy.mall.module.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子 VO（列表+详情共用）
 *
 * 对应接口:
 *   GET /api/post/list     → 列表（部分字段）
 *   GET /api/post/{id}     → 详情（全部字段）
 *
 * 📌 字段说明:
 *   authorNickname/authorAvatar → JOIN tb_user 获取
 *   isLiked/isCollected → 子查询判断当前用户是否点赞/收藏
 *   images → Service层从JSON字符串解析为List
 *   tags   → Service层从JSON字符串解析为List
 *   coverImage → images第一张，瀑布流卡片封面用
 */
@Data
public class PostVO {

    /** 帖子ID */
    private Long id;

    /** 发帖用户ID */
    private Long userId;

    /** 帖子标题 */
    private String title;

    /** 帖子正文（详情页用，列表不返回） */
    private String content;

    /** 图片列表（已从JSON解析） */
    private List<String> images;

    /** 封面图（images第一张，瀑布流卡片用） */
    private String coverImage;

    /** 标签列表（已从JSON解析） */
    private List<String> tags;

    /** 状态: 0草稿 1审核中 2已发布 3已拒绝 4管理员删除 */
    private Integer status;

    /** 拒绝原因（status=3时有值） */
    private String rejectReason;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 浏览数 */
    private Integer viewCount;

    /** 是否置顶 */
    private Integer isTop;

    /** 热度分 */
    private Double hotScore;

    /** 是否热门 */
    private Integer isHot;

    /** AI识别关联ID */
    private Long recognitionId;

    // ====== 作者信息（JOIN tb_user） ======

    /** 作者昵称 */
    private String authorNickname;

    /** 作者头像 */
    private String authorAvatar;

    /** 作者观测等级 */
    private Integer authorObservationLevel;

    // ====== 当前用户互动状态 ======

    /** 当前用户是否已点赞（未登录为false） */
    private Boolean isLiked;

    /** 当前用户是否已收藏（未登录为false） */
    private Boolean isCollected;

    /** 当前用户是否已关注作者（未登录为false） */
    private Boolean isFollowed;

    /** 发布时间 */
    private LocalDateTime createTime;
}

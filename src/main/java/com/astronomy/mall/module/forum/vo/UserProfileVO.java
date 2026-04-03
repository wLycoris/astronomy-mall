package com.astronomy.mall.module.forum.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户主页 VO（小红书风格）
 *
 * 对应接口: GET /api/user/profile/{userId}
 *
 * 📌 布局说明（小红书风格用户主页）:
 *   顶部: 星空背景Banner + 头像 + 昵称 + 观测等级Badge
 *   统计: 关注数 | 粉丝数 | 获赞与收藏
 *   操作: [+ 关注] [发消息]（他人主页时显示）
 *   Tab:  笔记 | 收藏 | 赞过（瀑布流卡片，复用PostCard组件）
 *
 * 📌 字段说明:
 *   postCount    → SELECT COUNT(*) FROM tb_post WHERE user_id=? AND deleted=0 AND status=2
 *   collectCount → SELECT COUNT(*) FROM tb_post_collect WHERE user_id=?
 *   followCount  → SELECT COUNT(*) FROM tb_user_follow WHERE follower_id=?
 *   fansCount    → SELECT COUNT(*) FROM tb_user_follow WHERE followed_id=?
 *   likeAndCollectCount → 获赞总数+收藏总数（所有帖子的like_count+collect_count之和）
 *   isFollowed   → 当前登录用户是否已关注此人
 */
@Data
public class UserProfileVO {

    /** 用户ID */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 观测经验等级(1-5) */
    private Integer observationLevel;

    /** 所在城市 */
    private String city;

    /** 发布帖子数（已发布且未删除） */
    private Integer postCount;

    /** 收藏帖子数 */
    private Integer collectCount;

    /** 关注数 */
    private Integer followCount;

    /** 粉丝数 */
    private Integer fansCount;

    /** 获赞与收藏总数（所有帖子的like_count+collect_count之和） */
    private Integer likeAndCollectCount;

    /** 当前登录用户是否已关注此人（未登录为false） */
    private Boolean isFollowed;

    /** 收藏列表是否公开(0-私密 1-公开) */
    private Integer collectVisible;

    /** 点赞列表是否公开(0-私密 1-公开) */
    private Integer likeVisible;

    /** 注册时间 */
    private LocalDateTime createTime;
}

package com.astronomy.mall.module.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 * 对应数据库表: tb_post
 *
 * 📌 状态说明:
 *   0=草稿  1=审核中  2=已发布  3=已拒绝  4=管理员删除
 *
 * 📌 images 字段:
 *   base64 JSON数组，最多9张，Canvas压缩(最长边1200px/quality=0.82)
 *   与二手回收模块图片方案一致
 *
 * 📌 tags 字段:
 *   JSON数组字符串，如 ["深空摄影","望远镜","星云"]
 *   推荐系统标签匹配核心字段（第16周联动）
 *
 * 📌 hot_score 字段:
 *   ForumScheduler 每小时计算: (likes×1 + comments×2 + collects×3) / (天数+2)^1.5
 *   is_hot: 热度超阈值自动置1，仅首次通知
 *
 * 📌 recognition_id 字段:
 *   AI识别结果分享时关联 tb_recognition.id，ForumPublish预填识别图片+天体标签
 */
@Data
@TableName("tb_post")
public class Post {

    /** 帖子ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发帖用户ID（关联tb_user.id） */
    private Long userId;

    /** 帖子标题 */
    private String title;

    /** 帖子正文 */
    private String content;

    /** 图片(base64 JSON数组，最多9张) */
    private String images;

    /** 帖子标签(JSON数组) */
    private String tags;

    /** 状态: 0草稿 1审核中 2已发布 3已拒绝 4管理员删除 */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 点赞数(冗余) */
    private Integer likeCount;

    /** 评论数(冗余) */
    private Integer commentCount;

    /** 收藏数(冗余) */
    private Integer collectCount;

    /** 浏览数 */
    private Integer viewCount;

    /** 是否置顶(管理员操作) */
    private Integer isTop;

    /** 热度分，每小时更新 */
    private Double hotScore;

    /** 是否热门(热度超阈值自动置1) */
    private Integer isHot;

    /** AI识别结果关联ID（关联tb_recognition.id） */
    private Long recognitionId;

    /** 逻辑删除(0正常 1删除) */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

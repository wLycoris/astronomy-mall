package com.astronomy.mall.module.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体类
 * 对应数据库表: tb_user_follow
 *
 * 📌 关注方向: follower_id 关注了 followed_id
 *   follower_id = 关注者（发起关注的人）
 *   followed_id = 被关注者（被关注的人）
 *
 * 📌 唯一约束: uk_follow(follower_id, followed_id)
 *   同一用户不能重复关注同一个人
 *   关注/取消关注通过 INSERT/DELETE 实现（幂等切换）
 *
 * 📌 索引说明:
 *   idx_follower  → 查"我关注的人"列表
 *   idx_followed  → 查"关注我的人"(粉丝)列表
 */
@Data
@TableName("tb_user_follow")
public class UserFollow {

    /** 关注关系ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注者ID（关联tb_user.id） */
    private Long followerId;

    /** 被关注者ID（关联tb_user.id） */
    private Long followedId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

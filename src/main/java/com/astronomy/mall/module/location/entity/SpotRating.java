package com.astronomy.mall.module.location.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 观测点用户评分记录实体类
 * 对应数据库表: tb_spot_rating
 *
 * 📌 防重复评分:
 *   数据库唯一约束 uk_user_spot (user_id, spot_id)
 *   每个用户对每个观测点只能评一次分，不允许修改
 *
 * 📌 评分同步:
 *   每次新增评分记录后，Service层必须重新计算并更新
 *   tb_observation_spot.rating（平均分）和 rating_count（评分人数）
 */
@Data
@TableName("tb_spot_rating")
public class SpotRating {

    /** 评分ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID (关联tb_user.id) */
    private Long userId;

    /** 观测点ID (关联tb_observation_spot.id) */
    private Long spotId;

    /** 评分(1-5星) */
    private Integer score;

    /** 评分时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
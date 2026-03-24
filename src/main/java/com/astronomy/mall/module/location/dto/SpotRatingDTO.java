package com.astronomy.mall.module.location.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 观测点评分请求 DTO
 *
 * 对应接口: POST /api/location/spot/{id}/rating
 * 业务规则:
 *   - 每个用户对同一观测点只能评分一次（tb_spot_rating UNIQUE KEY uk_user_spot(user_id, spot_id)）
 *   - 评分范围: 1-5 整数
 *   - 提交后立即重新计算 tb_observation_spot.rating 和 rating_count
 *
 * 防重复机制: MySQL UNIQUE约束 + 业务层提前查询判断（给出友好提示）
 */
@Data
public class SpotRatingDTO {

    /**
     * 评分（1-5星）
     * 1=很差, 2=较差, 3=一般, 4=不错, 5=非常好
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer score;
}
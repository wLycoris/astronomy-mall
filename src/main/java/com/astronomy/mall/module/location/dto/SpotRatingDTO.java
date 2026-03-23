package com.astronomy.mall.module.location.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 观测点评分请求DTO
 * 对应接口: POST /api/location/spot/{id}/rating
 *
 * 📌 6.0 骨架，接口逻辑在 6.1 节实现
 * 📌 防重复：数据库唯一约束 uk_user_spot，重复提交返回业务异常
 */
@Data
public class SpotRatingDTO {

    /**
     * 评分（1-5星，必填）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer score;
}
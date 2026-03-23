package com.astronomy.mall.module.location.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 用户签到请求DTO
 * 对应接口: POST /api/location/checkin
 *
 * 📌 6.0 骨架，接口逻辑在 6.3 节实现
 */
@Data
public class CheckinDTO {

    /**
     * 观测点ID（必填）
     */
    @NotNull(message = "观测点ID不能为空")
    private Long spotId;

    /**
     * 签到时用户GPS经度（可选，前端定位成功后传入）
     * 高德GCJ-02坐标系
     */
    private BigDecimal longitude;

    /**
     * 签到时用户GPS纬度（可选）
     * 高德GCJ-02坐标系
     */
    private BigDecimal latitude;
}
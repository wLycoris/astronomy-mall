package com.astronomy.mall.module.location.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 编辑观测点 DTO（管理员端）
 * 6.5 后台观测点管理
 *
 * 与 SpotCreateDTO 字段完全一致，但所有字段均为可选（仅更新传入的字段）
 */
@Data
public class SpotUpdateDTO {

    /** 观测点名称 */
    @Size(max = 100, message = "观测点名称最多100字")
    private String spotName;

    /** 经度(高德GCJ-02坐标系) */
    @DecimalMin(value = "-180.0", message = "经度范围-180~180")
    @DecimalMax(value = "180.0", message = "经度范围-180~180")
    private BigDecimal longitude;

    /** 纬度(高德GCJ-02坐标系) */
    @DecimalMin(value = "-90.0", message = "纬度范围-90~90")
    @DecimalMax(value = "90.0", message = "纬度范围-90~90")
    private BigDecimal latitude;

    /** 省份 */
    @Size(max = 50, message = "省份最多50字")
    private String province;

    /** 城市 */
    @Size(max = 50, message = "城市最多50字")
    private String city;

    /** 详细地址 */
    @Size(max = 200, message = "地址最多200字")
    private String address;

    /** 海拔(米) */
    @Min(value = 0, message = "海拔不能为负")
    private Integer altitude;

    /** Bortle暗天等级(1-9，越小越暗越好) */
    @Min(value = 1, message = "Bortle等级最小为1")
    @Max(value = 9, message = "Bortle等级最大为9")
    private Integer lightPollutionLevel;

    /** 观测点描述 */
    private String description;

    /** 图片URL列表（JSON数组字符串） */
    private String images;
}

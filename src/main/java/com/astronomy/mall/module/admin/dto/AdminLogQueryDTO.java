package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理员操作日志查询DTO
 *
 * 📌 对应接口: GET /api/admin/log/list
 * 📌 支持筛选: 操作类型 / 管理员 / 时间范围 / 状态
 */
@Data
@ApiModel("管理员操作日志查询DTO")
public class AdminLogQueryDTO {

    /**
     * 管理员ID（精确匹配）
     */
    @ApiModelProperty("管理员ID")
    private Long adminId;

    /**
     * 管理员姓名（模糊搜索）
     */
    @ApiModelProperty("管理员姓名（模糊）")
    private String adminName;

    /**
     * 操作类型（模糊搜索，如"商品上架"、"退款审核"）
     */
    @ApiModelProperty("操作类型（模糊）")
    private String operation;

    /**
     * 状态：0-失败  1-成功  null-全部
     */
    @ApiModelProperty("状态：0失败 1成功")
    private Integer status;

    /**
     * 开始时间（yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     */
    @ApiModelProperty("开始时间")
    private String startTime;

    /**
     * 结束时间（yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     */
    @ApiModelProperty("结束时间")
    private String endTime;

    /**
     * 当前页，默认第1页
     */
    @ApiModelProperty("当前页（默认1）")
    private Integer pageNum = 1;

    /**
     * 每页条数，默认20条
     */
    @ApiModelProperty("每页条数（默认20）")
    private Integer pageSize = 20;
}
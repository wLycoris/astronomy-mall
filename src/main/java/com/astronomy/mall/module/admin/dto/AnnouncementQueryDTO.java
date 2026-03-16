package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统公告查询 DTO（分页）
 *
 * 📌 说明:
 * 公告列表通过 GROUP BY related_id 从 tb_notification 中聚合展示，
 * 每条公告对应多条通知记录（每个用户一条）。
 *
 * 文件路径: com.astronomy.mall.module.admin.dto.AnnouncementQueryDTO
 */
@Data
@ApiModel(description = "公告列表查询参数")
public class AnnouncementQueryDTO {

    /**
     * 页码（默认第1页）
     */
    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页数量（默认10条）
     */
    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;

    /**
     * 关键词搜索（搜索公告标题）
     */
    @ApiModelProperty(value = "关键词（搜索标题）", example = "维护")
    private String keyword;

    /**
     * 优先级筛选（0-普通 1-重要 2-紧急，不传则查全部）
     */
    @ApiModelProperty(value = "优先级(0-普通 1-重要 2-紧急)", example = "1")
    private Integer priority;

    /**
     * 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    @ApiModelProperty(value = "开始时间", example = "2026-01-01 00:00:00")
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    @ApiModelProperty(value = "结束时间", example = "2026-12-31 23:59:59")
    private String endTime;
}
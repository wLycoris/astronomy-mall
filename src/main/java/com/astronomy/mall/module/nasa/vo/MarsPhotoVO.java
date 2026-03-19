package com.astronomy.mall.module.nasa.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 火星车拍摄照片 VO
 *
 * 📌 对应 NASA Mars Rover Photos API:
 *    https://api.nasa.gov/mars-photos/api/v1/rovers/{rover}/latest_photos
 * 📌 rover 优先级: Perseverance → Curiosity（Perseverance 返回空时降级）
 * 📌 供 MarsRoverSyncScheduler 使用（课程模块「火星探测车日志」自动同步）
 * 📌 每次取前3张照片作为新章节
 */
@Data
@ApiModel(description = "火星车拍摄照片VO")
public class MarsPhotoVO {

    /**
     * 照片图片 URL
     * NASA 返回的 img_src 字段（JPG 格式）
     */
    @ApiModelProperty("图片URL")
    private String imgSrc;

    /**
     * 拍摄摄像头全称
     * 示例: "Front Hazard Avoidance Camera", "Mast Camera"
     */
    @ApiModelProperty("摄像头全称")
    private String cameraFullName;

    /**
     * 地球日期，格式: yyyy-MM-dd
     * 示例: "2024-03-15"
     */
    @ApiModelProperty("地球日期")
    private String earthDate;
}
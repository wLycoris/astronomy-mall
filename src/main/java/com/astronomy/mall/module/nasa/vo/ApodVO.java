package com.astronomy.mall.module.nasa.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * NASA 每日天文图片 (APOD) 返回VO
 *
 * 📌 对应 NASA APOD API: https://api.nasa.gov/planetary/apod
 * 📌 mediaType 取值: "image" 或 "video"
 * 📌 video 类型的 url 通常是 YouTube embed 地址（iframe 可直接使用）
 * 📌 供 APODSyncScheduler 和商城首页 ApodCard 共用
 */
@Data
@ApiModel(description = "NASA每日天文图片VO")
public class ApodVO {

    /**
     * 日期，格式: yyyy-MM-dd
     * 示例: "2024-03-15"
     */
    @ApiModelProperty("日期")
    private String date;

    /**
     * 标题（英文）
     * 课程模块 APODSyncScheduler 用此字段作为章节标题
     */
    @ApiModelProperty("标题")
    private String title;

    /**
     * 详细说明文字（英文长文）
     * 课程模块 APODSyncScheduler 用此字段作为章节内容
     */
    @ApiModelProperty("说明文字")
    private String explanation;

    /**
     * 图片/视频 URL（标清）
     * - image 类型: 直接图片链接
     * - video 类型: YouTube embed 链接，如 https://www.youtube.com/embed/xxx
     */
    @ApiModelProperty("标清URL")
    private String url;

    /**
     * 高清图片 URL（仅 image 类型才有，video 类型为 null）
     */
    @ApiModelProperty("高清URL（图片类型专用）")
    private String hdurl;

    /**
     * 媒体类型: "image" 或 "video"
     * 前端根据此字段决定渲染 <el-image> 还是 <iframe>
     */
    @ApiModelProperty("媒体类型: image/video")
    private String mediaType;

    /**
     * 版权信息（可为 null，NASA 自拍图片通常无版权）
     */
    @ApiModelProperty("版权（可为空）")
    private String copyright;
}
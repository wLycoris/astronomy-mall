package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告 VO（列表 & 详情通用）
 *
 * 📌 数据来源说明:
 * 由 tb_notification 表 GROUP BY related_id 聚合得到：
 *   - announcementId  ← related_id（公告分组ID）
 *   - title           ← MIN(title)（同组所有通知标题相同）
 *   - content         ← MIN(content)（同组所有通知内容相同）
 *   - priority        ← MIN(priority)（同组所有通知优先级相同）
 *   - sendCount       ← COUNT(*)（本次公告发送总人数）
 *   - readCount       ← SUM(is_read)（已读人数）
 *   - readRate        ← 已读率 = readCount / sendCount * 100
 *   - createTime      ← MIN(create_time)（公告发布时间）
 *
 * 文件路径: com.astronomy.mall.module.admin.vo.AnnouncementVO
 */
@Data
@ApiModel(description = "系统公告VO")
public class AnnouncementVO {

    /**
     * 公告ID（即 tb_notification.related_id，用于详情/删除接口）
     */
    @ApiModelProperty("公告ID（related_id）")
    private Long announcementId;

    /**
     * 公告标题
     */
    @ApiModelProperty("公告标题")
    private String title;

    /**
     * 公告内容
     */
    @ApiModelProperty("公告内容")
    private String content;

    /**
     * 优先级（0-普通 1-重要 2-紧急）
     */
    @ApiModelProperty("优先级(0-普通 1-重要 2-紧急)")
    private Integer priority;

    /**
     * 优先级展示文本（前端展示用）
     */
    @ApiModelProperty("优先级文本")
    private String priorityText;

    /**
     * 发送人数（本次公告发送给多少用户）
     */
    @ApiModelProperty("发送人数")
    private Integer sendCount;

    /**
     * 已读人数
     */
    @ApiModelProperty("已读人数")
    private Integer readCount;

    /**
     * 已读率（百分比，保留1位小数，例如 75.5）
     */
    @ApiModelProperty("已读率（百分比）")
    private Double readRate;

    /**
     * 公告发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("发布时间")
    private LocalDateTime createTime;
}
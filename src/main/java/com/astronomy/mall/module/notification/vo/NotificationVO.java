package com.astronomy.mall.module.notification.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知VO
 */
@Data
@ApiModel("通知VO")
public class NotificationVO {

    @ApiModelProperty("通知ID")
    private Long id;

    @ApiModelProperty("所属模块")
    private String module;

    @ApiModelProperty("模块名称")
    private String moduleName;

    @ApiModelProperty("通知类型")
    private String type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("通知标题")
    private String title;

    @ApiModelProperty("通知内容")
    private String content;

    @ApiModelProperty("关联业务ID")
    private Long relatedId;

    @ApiModelProperty("关联类型")
    private String relatedType;

    @ApiModelProperty("跳转链接")
    private String jumpUrl;

    @ApiModelProperty("是否已读(0-未读 1-已读)")
    private Integer isRead;

    @ApiModelProperty("阅读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @ApiModelProperty("优先级(0-普通 1-重要 2-紧急)")
    private Integer priority;

    @ApiModelProperty("扩展数据")
    private String extraData;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
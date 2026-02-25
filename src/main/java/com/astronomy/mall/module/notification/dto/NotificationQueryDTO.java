package com.astronomy.mall.module.notification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通知查询DTO
 */
@Data
@ApiModel("通知查询DTO")
public class NotificationQueryDTO {

    @ApiModelProperty("页码")
    private Integer pageNum = 1;

    @ApiModelProperty("每页大小")
    private Integer pageSize = 10;

    @ApiModelProperty("模块(不传查全部)")
    private String module;

    @ApiModelProperty("是否已读(0-未读 1-已读,不传查全部)")
    private Integer isRead;

    @ApiModelProperty("通知类型")
    private String type;
}
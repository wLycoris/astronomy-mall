package com.astronomy.mall.module.notification.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知设置VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("通知设置VO")
public class NotificationSettingVO {

    @ApiModelProperty("模块")
    private String module;

    @ApiModelProperty("模块名称")
    private String moduleName;

    @ApiModelProperty("通知类型")
    private String type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("是否启用(0-关闭 1-开启)")
    private Integer enabled;
}
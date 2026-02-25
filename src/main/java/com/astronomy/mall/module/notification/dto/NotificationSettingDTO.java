package com.astronomy.mall.module.notification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 通知设置DTO
 */
@Data
@ApiModel("通知设置DTO")
public class NotificationSettingDTO {

    @ApiModelProperty(value = "模块", required = true)
    @NotBlank(message = "模块不能为空")
    private String module;

    @ApiModelProperty(value = "通知类型", required = true)
    @NotBlank(message = "通知类型不能为空")
    private String type;

    @ApiModelProperty(value = "是否启用(0-关闭 1-开启)", required = true)
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;
}
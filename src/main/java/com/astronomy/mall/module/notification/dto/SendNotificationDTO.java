package com.astronomy.mall.module.notification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 发送通知DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("发送通知DTO")
public class SendNotificationDTO {

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "模块", required = true)
    @NotBlank(message = "模块不能为空")
    private String module;

    @ApiModelProperty(value = "通知类型", required = true)
    @NotBlank(message = "通知类型不能为空")
    private String type;

    @ApiModelProperty("关联业务ID")
    private Long relatedId;

    @ApiModelProperty("关联类型")
    private String relatedType;

    @ApiModelProperty("优先级(0-普通 1-重要 2-紧急)")
    private Integer priority = 0;

    @ApiModelProperty("模板变量(用于替换模板中的占位符)")
    private Map<String, Object> variables;

    @ApiModelProperty("扩展数据(JSON格式)")
    private Map<String, Object> extraData;
}
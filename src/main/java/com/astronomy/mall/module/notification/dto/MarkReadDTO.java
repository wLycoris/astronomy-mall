package com.astronomy.mall.module.notification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 标记已读DTO
 */
@Data
@ApiModel("标记已读DTO")
public class MarkReadDTO {

    @ApiModelProperty(value = "通知ID列表", required = true)
    @NotEmpty(message = "通知ID列表不能为空")
    private List<Long> ids;
}
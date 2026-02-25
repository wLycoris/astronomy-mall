package com.astronomy.mall.module.notification.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读数统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("未读数统计VO")
public class UnreadCountVO {

    @ApiModelProperty("总未读数")
    private Long total;

    @ApiModelProperty("商城模块未读数")
    private Long mall;

    @ApiModelProperty("论坛模块未读数")
    private Long forum;

    @ApiModelProperty("课程模块未读数")
    private Long course;

    @ApiModelProperty("地理位置模块未读数")
    private Long location;

    @ApiModelProperty("推荐系统未读数")
    private Long recommend;

    @ApiModelProperty("AI识别模块未读数")
    private Long ai;

    @ApiModelProperty("系统模块未读数")
    private Long system;
}
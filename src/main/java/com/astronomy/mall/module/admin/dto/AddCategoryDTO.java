package com.astronomy.mall.module.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 新增/编辑分类 DTO
 *
 * 📌 用于接收前端提交的分类数据
 * 📌 add 和 update 接口共用此 DTO
 */
@Data
@ApiModel("新增/编辑分类DTO")
public class AddCategoryDTO {

    /**
     * 分类名称（必填）
     */
    @NotBlank(message = "分类名称不能为空")
    @ApiModelProperty(value = "分类名称", required = true, example = "望远镜")
    private String categoryName;

    /**
     * 父分类ID
     * 📌 0 表示一级分类，非0 表示二级分类
     */
    @NotNull(message = "父分类ID不能为空")
    @ApiModelProperty(value = "父分类ID（0=一级分类，非0=二级分类的父ID）", required = true, example = "0")
    private Long parentId;

    /**
     * 分类图标 URL（选填）
     */
    @ApiModelProperty(value = "分类图标URL", example = "https://example.com/icon.png")
    private String icon;

    /**
     * 排序值，数字越大越靠前（选填，默认0）
     */
    @ApiModelProperty(value = "排序值（数字越大越靠前）", example = "10")
    private Integer sort;

    /**
     * 分类描述（选填）
     */
    @ApiModelProperty(value = "分类描述", example = "各类天文望远镜")
    private String description;

    /**
     * 是否显示：0-隐藏，1-显示（默认1）
     */
    @ApiModelProperty(value = "是否显示（0-隐藏 1-显示）", example = "1")
    private Integer isShow;
}
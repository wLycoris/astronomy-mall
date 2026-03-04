package com.astronomy.mall.module.admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类树形结构 VO
 *
 * 📌 用于后台管理分类树列表展示
 * 📌 children 字段存放二级分类
 */
@Data
@ApiModel("分类树VO")
public class CategoryTreeVO {

    /**
     * 分类ID
     */
    @ApiModelProperty("分类ID")
    private Long id;

    /**
     * 分类名称
     */
    @ApiModelProperty("分类名称")
    private String categoryName;

    /**
     * 父分类ID（0=一级分类）
     */
    @ApiModelProperty("父分类ID（0=一级分类）")
    private Long parentId;

    /**
     * 分类层级：1=一级，2=二级
     */
    @ApiModelProperty("分类层级（1=一级 2=二级）")
    private Integer level;

    /**
     * 分类图标URL
     */
    @ApiModelProperty("分类图标URL")
    private String icon;

    /**
     * 排序值（数字越大越靠前）
     */
    @ApiModelProperty("排序值")
    private Integer sort;

    /**
     * 分类描述
     */
    @ApiModelProperty("分类描述")
    private String description;

    /**
     * 是否显示：0-隐藏，1-显示
     */
    @ApiModelProperty("是否显示（0-隐藏 1-显示）")
    private Integer isShow;

    /**
     * 该分类下的商品数量
     * 📌 用于删除前检查关联商品
     */
    @ApiModelProperty("关联商品数量")
    private Integer productCount;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    /**
     * 子分类列表（仅一级分类有此字段）
     * 📌 二级分类的 children 为空
     */
    @ApiModelProperty("子分类列表（仅一级分类有）")
    private List<CategoryTreeVO> children;
}
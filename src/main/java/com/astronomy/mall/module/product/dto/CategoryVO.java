package com.astronomy.mall.module.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(description = "分类返回VO")
public class CategoryVO {

    @ApiModelProperty("分类ID")
    private Long id;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("父分类ID")
    private Long parentId;

    @ApiModelProperty("分类层级")
    private Integer level;

    @ApiModelProperty("分类图标URL")
    private String icon;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("子分类列表")
    private List<CategoryVO> children;
}
package com.astronomy.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_category")
@ApiModel(description = "商品分类实体")
public class Category {

    @ApiModelProperty("分类ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("父分类ID(0表示一级分类)")
    private Long parentId;

    @ApiModelProperty("分类层级(1-一级 2-二级)")
    private Integer level;

    @ApiModelProperty("分类图标URL")
    private String icon;

    @ApiModelProperty("排序(数字越大越靠前)")
    private Integer sort;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("是否显示(0-否 1-是)")
    private Integer isShow;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除(0-否 1-是)")
    @TableLogic
    private Integer deleted;
}
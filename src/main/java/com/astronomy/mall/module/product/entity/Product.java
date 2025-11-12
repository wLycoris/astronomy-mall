package com.astronomy.mall.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_product")
@ApiModel(description = "商品实体")
public class Product {

    @ApiModelProperty("商品ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("副标题")
    private String subTitle;

    @ApiModelProperty("品牌")
    private String brand;

    @ApiModelProperty("价格")
    private BigDecimal price;

    @ApiModelProperty("原价")
    private BigDecimal originalPrice;

    @ApiModelProperty("库存")
    private Integer stock;

    @ApiModelProperty("销量")
    private Integer sales;

    @ApiModelProperty("主图URL")
    private String mainImage;

    @ApiModelProperty("商品图片(多张,逗号分隔)")
    private String images;

    @ApiModelProperty("商品详情(富文本)")
    private String detail;

    @ApiModelProperty("规格参数(JSON格式)")
    private String specifications;

    @ApiModelProperty("商品状态(0-下架 1-上架)")
    private Integer status;

    @ApiModelProperty("是否推荐(0-否 1-是)")
    private Integer isRecommend;

    @ApiModelProperty("是否热卖(0-否 1-是)")
    private Integer isHot;

    @ApiModelProperty("是否新品(0-否 1-是)")
    private Integer isNew;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;

    @ApiModelProperty("搜索关键词(用于推荐算法)")
    private String keywords;

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
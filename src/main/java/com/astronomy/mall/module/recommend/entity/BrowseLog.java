package com.astronomy.mall.module.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品浏览记录实体
 * 推荐系统 CF 数据源之一，记录用户浏览商品行为
 * Redis SETNX "browse:dedup:{userId}:{productId}" TTL=1800s 防刷
 */
@Data
@TableName("tb_browse_log")
@ApiModel(description = "商品浏览记录")
public class BrowseLog {

    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("分类ID(冗余,加速按类推荐)")
    private Long categoryId;

    @ApiModelProperty("来源: detail/list")
    private String source;

    @ApiModelProperty("浏览时间")
    private LocalDateTime browseTime;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

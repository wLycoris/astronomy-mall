package com.astronomy.mall.module.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子浏览记录实体
 * 推荐系统内容相似度数据源，记录用户浏览帖子行为
 * Redis SETNX "post:browse:dedup:{userId}:{postId}" TTL=1800s 防刷
 */
@Data
@TableName("tb_post_browse_log")
@ApiModel(description = "帖子浏览记录")
public class PostBrowseLog {

    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("帖子ID")
    private Long postId;

    @ApiModelProperty("浏览时间")
    private LocalDateTime browseTime;

    @ApiModelProperty("浏览时长(秒)")
    private Integer duration;

    @ApiModelProperty("来源页面")
    private String source;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.astronomy.mall.module.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐曝光与点击记录实体
 * 用途：答辩时现场展示曝光总量/点击率，无需做 A/B test
 * 写入：所有推荐接口返回前异步批量 INSERT
 * 点击：前端点击推荐卡片 → POST /api/recommend/click 回写 is_clicked + click_time
 */
@Data
@TableName("tb_recommend_record")
@ApiModel(description = "推荐曝光与点击记录")
public class RecommendRecord {

    @ApiModelProperty("主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("推荐类型: product/course/post")
    private String recommendType;

    @ApiModelProperty("被推荐的目标ID")
    private Long targetId;

    @ApiModelProperty("算法: content/collaborative/hot/coldstart")
    private String algorithm;

    @ApiModelProperty("推荐得分")
    private Double score;

    @ApiModelProperty("推荐位置(1-10)")
    private Integer position;

    @ApiModelProperty("0-未点击 1-已点击")
    private Integer isClicked;

    @ApiModelProperty("点击时间")
    private LocalDateTime clickTime;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

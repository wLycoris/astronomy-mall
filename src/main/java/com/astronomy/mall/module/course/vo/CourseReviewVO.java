package com.astronomy.mall.module.course.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程评价 VO（用户端展示）
 * 接口: GET /api/course/{courseId}/reviews
 *       GET /api/course/{courseId}/review/my
 */
@Data
@ApiModel(description = "课程评价 VO（用户端）")
public class CourseReviewVO {

    @ApiModelProperty("评价ID")
    private Long id;

    @ApiModelProperty("课程ID")
    private Long courseId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("评分(1-5星)")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("课程标题（我的评价页使用）")
    private String courseTitle;

    @ApiModelProperty("课程封面（我的评价页使用）")
    private String courseCover;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
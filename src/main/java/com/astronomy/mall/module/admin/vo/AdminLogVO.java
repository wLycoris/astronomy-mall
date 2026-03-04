package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志VO
 *
 * 📌 用于列表展示和详情查看
 * 📌 前端接收字段，不暴露内部实体类
 */
@Data
@ApiModel("管理员操作日志VO")
public class AdminLogVO {

    @ApiModelProperty("日志ID")
    private Long id;

    @ApiModelProperty("管理员ID")
    private Long adminId;

    @ApiModelProperty("管理员姓名")
    private String adminName;

    /**
     * 操作类型（如"商品上架"、"订单发货"）
     */
    @ApiModelProperty("操作类型")
    private String operation;

    /**
     * 请求方法（全限定类名.方法名）
     * 详情页展示，列表页可省略
     */
    @ApiModelProperty("请求方法")
    private String method;

    /**
     * 请求参数（JSON格式）
     * 详情页展示
     */
    @ApiModelProperty("请求参数(JSON)")
    private String params;

    /**
     * 客户端IP
     */
    @ApiModelProperty("IP地址")
    private String ipAddress;

    /**
     * User-Agent（浏览器信息）
     */
    @ApiModelProperty("User-Agent")
    private String userAgent;

    /**
     * 执行耗时（毫秒）
     */
    @ApiModelProperty("执行耗时(ms)")
    private Integer executionTime;

    /**
     * 操作状态：0-失败  1-成功
     */
    @ApiModelProperty("状态：0失败 1成功")
    private Integer status;

    /**
     * 状态文字（前端展示用）
     * "成功" / "失败"
     */
    @ApiModelProperty("状态文字")
    private String statusText;

    /**
     * 错误信息（失败时有值）
     */
    @ApiModelProperty("错误信息")
    private String errorMsg;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("操作时间")
    private LocalDateTime createTime;
}
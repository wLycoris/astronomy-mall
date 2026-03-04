package com.astronomy.mall.module.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员操作日志实体
 *
 * 📌 对应表: tb_admin_log
 * 📌 由 AdminLogAspect AOP 切面自动填充并保存
 * 📌 字段说明:
 *   - user_agent / execution_time / error_msg 为扩展字段，需执行 ALTER TABLE 添加
 *   - 若未执行ALTER，这三个字段不影响核心功能（INSERT时忽略不存在的列会报错，请先执行建表/ALTER）
 *
 * ⚠️ 需执行以下ALTER语句为tb_admin_log添加扩展字段（如果当前表缺少这些列）：
 * ALTER TABLE `tb_admin_log`
 *   ADD COLUMN `user_agent`     VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent' AFTER `ip_address`,
 *   ADD COLUMN `execution_time` INT(11)      DEFAULT NULL COMMENT '执行耗时(ms)' AFTER `user_agent`,
 *   ADD COLUMN `error_msg`      VARCHAR(500) DEFAULT NULL COMMENT '错误信息'    AFTER `execution_time`;
 */
@Data
@TableName("tb_admin_log")
@ApiModel("管理员操作日志")
public class AdminLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("日志ID")
    private Long id;

    /** 管理员ID */
    @ApiModelProperty("管理员ID")
    private Long adminId;

    /** 管理员姓名 */
    @ApiModelProperty("管理员姓名")
    private String adminName;

    /**
     * 操作类型
     * 示例: "商品上架"、"订单发货"、"退款审核通过"、"禁用用户"
     */
    @ApiModelProperty("操作类型")
    private String operation;

    /**
     * 请求方法（全限定类名.方法名）
     * 示例: com.astronomy.mall.module.admin.controller.AdminProductController.updateStatus
     */
    @ApiModelProperty("请求方法")
    private String method;

    /**
     * 请求参数（JSON格式，超过2000字截断）
     */
    @ApiModelProperty("请求参数(JSON)")
    private String params;

    /**
     * 客户端IP地址
     */
    @ApiModelProperty("IP地址")
    private String ipAddress;

    /**
     * User-Agent（浏览器/客户端信息）
     */
    @ApiModelProperty("User-Agent")
    private String userAgent;

    /**
     * 操作状态：0-失败  1-成功
     */
    @ApiModelProperty("状态：0失败 1成功")
    private Integer status;

    /**
     * 错误信息（操作失败时记录）
     */
    @ApiModelProperty("错误信息")
    private String errorMsg;

    /**
     * 执行耗时（毫秒）
     */
    @ApiModelProperty("执行耗时(ms)")
    private Integer executionTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
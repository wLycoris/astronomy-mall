package com.astronomy.mall.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@TableName("tb_login_log")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 登录时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime loginTime;

    /** 登录IP */
    private String ipAddress;

    /** 登录设备 */
    private String device;

    /** 登录状态(0-失败 1-成功) */
    private Integer status;

    /** 登录信息 */
    private String message;
}
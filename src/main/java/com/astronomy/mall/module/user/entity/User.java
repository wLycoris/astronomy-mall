package com.astronomy.mall.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("tb_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码(MD5加密) */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 兴趣标签(JSON数组) */
    private String interestTags;

    /** 观测经验等级(1-5) */
    private Integer observationLevel;

    /** 所在城市 */
    private String city;

    /** 所在省份 */
    private String province;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 角色(0-普通用户 1-管理员) */
    private Integer role;

    /** 状态(0-禁用 1-启用) */
    private Integer status;

    /** 逻辑删除(0-未删除 1-已删除) */
    @TableLogic
    private Integer deleted;

    /** 注册时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    private BigDecimal balance;
}
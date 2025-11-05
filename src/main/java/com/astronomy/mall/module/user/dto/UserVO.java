package com.astronomy.mall.module.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户信息VO(返回给前端的数据)
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String interestTags;
    private Integer observationLevel;
    private String city;
    private String province;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer role;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;

    // 不返回密码等敏感信息
}
package com.astronomy.mall.module.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回VO
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT Token */
    private String token;

    /** 用户信息 */
    private UserVO userInfo;
}
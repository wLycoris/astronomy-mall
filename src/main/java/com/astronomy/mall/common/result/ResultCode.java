package com.astronomy.mall.common.result;

import lombok.Getter;

/**
 * 返回码枚举
 */
@Getter
public enum ResultCode {

    // 通用
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 用户相关 (1xxx)
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    USERNAME_EXISTS(1005, "用户名已被使用"),
    EMAIL_EXISTS(1006, "邮箱已被注册"),
    PHONE_EXISTS(1007, "手机号已被注册"),
    TOKEN_INVALID(1008, "Token无效或已过期"),
    TOKEN_EXPIRED(1009, "Token已过期"),

    // 商品相关 (2xxx)
    PRODUCT_NOT_EXIST(2001, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "商品库存不足"),

    // 订单相关 (3xxx)
    ORDER_NOT_EXIST(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
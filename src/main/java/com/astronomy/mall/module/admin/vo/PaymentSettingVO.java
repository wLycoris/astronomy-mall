package com.astronomy.mall.module.admin.vo;

import lombok.Data;

/**
 * 支付设置响应 VO
 * 接口: GET /api/admin/setting/payment
 */
@Data
public class PaymentSettingVO {

    /** 支付宝是否开启 */
    private Boolean alipayEnabled;

    /** 微信支付是否开启 */
    private Boolean wechatEnabled;

    /** 余额支付是否开启 */
    private Boolean balanceEnabled;

    /** 支付超时时间(分钟) */
    private Integer payTimeoutMinutes;

    /** 自动确认收货天数 */
    private Integer autoConfirmDays;

    /** 超时未支付自动关闭天数 */
    private Integer autoCloseDays;
}
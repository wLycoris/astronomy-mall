package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 更新支付设置请求 DTO
 * 接口: PUT /api/admin/setting/payment
 */
@Data
public class PaymentSettingDTO {

    /** 是否开启支付宝支付 */
    private Boolean alipayEnabled;

    /** 是否开启微信支付 */
    private Boolean wechatEnabled;

    /** 是否开启余额支付 */
    private Boolean balanceEnabled;

    /**
     * 支付超时时间(分钟)，最小 5 分钟
     */
    @NotNull(message = "支付超时时间不能为空")
    @Min(value = 5, message = "支付超时时间最少5分钟")
    private Integer payTimeoutMinutes;

    /**
     * 发货后自动确认收货天数
     * 超过该天数系统自动将订单标为已完成
     */
    @NotNull(message = "自动确认收货天数不能为空")
    @Min(value = 1, message = "自动确认天数最少1天")
    private Integer autoConfirmDays;

    /**
     * 超时未支付自动关闭天数
     */
    @NotNull(message = "自动关闭天数不能为空")
    @Min(value = 1, message = "自动关闭天数最少1天")
    private Integer autoCloseDays;
}
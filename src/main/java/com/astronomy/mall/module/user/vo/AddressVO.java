package com.astronomy.mall.module.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址 VO
 * 接口返回给前端的地址数据视图对象
 *
 * 📌 前端使用场景:
 * 1. 个人中心地址列表 (AddressManage.vue)
 * 2. 结算页地址选择 (CheckoutPage.vue)
 */
@Data
public class AddressVO {

    /** 地址ID */
    private Long id;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人手机号 */
    private String receiverPhone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区县 */
    private String district;

    /** 详细地址 */
    private String detail;

    /**
     * 是否默认地址
     * 0 - 否
     * 1 - 是
     */
    private Integer isDefault;

    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 完整地址（省市区 + 详细地址的拼接，方便前端展示）
     * 示例: "广东省 深圳市 南山区 科技园南路XX号"
     */
    public String getFullAddress() {
        return province + " " + city + " " + district + " " + detail;
    }
}
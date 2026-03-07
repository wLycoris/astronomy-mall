package com.astronomy.mall.module.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 收货地址 DTO
 * 用于新增地址 (POST /api/address/add) 和
 *     编辑地址 (PUT  /api/address/update/:id)
 *
 * 📌 字段校验说明:
 * - receiverName: 必填，1-50字
 * - receiverPhone: 必填，11位手机号格式
 * - province/city/district: 必填
 * - detail: 必填，最长200字
 * - isDefault: 可选，0或1，不传默认为0（不设为默认）
 */
@Data
public class AddressDTO {

    /** 收货人姓名 */
    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名最长50个字符")
    private String receiverName;

    /** 收货人手机号 */
    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    /** 省份 */
    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份名称最长50个字符")
    private String province;

    /** 城市 */
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市名称最长50个字符")
    private String city;

    /** 区县 */
    @NotBlank(message = "区县不能为空")
    @Size(max = 50, message = "区县名称最长50个字符")
    private String district;

    /** 详细地址 */
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址最长200个字符")
    private String detail;

    /**
     * 是否设为默认地址
     * 0 - 否（默认）
     * 1 - 是
     */
    private Integer isDefault;
}
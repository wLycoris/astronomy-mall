package com.astronomy.mall.module.order.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderDTO {

    @NotEmpty(message = "购物车ID列表不能为空")
    private List<Long> cartIds; // 从购物车创建订单

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    @NotBlank(message = "收货省份不能为空")
    private String receiverProvince;

    @NotBlank(message = "收货城市不能为空")
    private String receiverCity;

    @NotBlank(message = "收货区县不能为空")
    private String receiverDistrict;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址不能超过200个字符")
    private String receiverAddress;

    @Size(max = 200, message = "订单备注不能超过200个字符")
    private String remark;
}
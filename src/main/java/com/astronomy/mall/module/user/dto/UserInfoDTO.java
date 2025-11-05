package com.astronomy.mall.module.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 用户信息更新DTO
 */
@Data
public class UserInfoDTO {

    @Size(max = 50, message = "昵称长度不能超过50位")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String avatar;

    private String interestTags;

    private Integer observationLevel;

    private String city;

    private String province;

    private BigDecimal longitude;

    private BigDecimal latitude;
}
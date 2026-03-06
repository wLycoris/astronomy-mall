package com.astronomy.mall.module.admin.dto;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 更新运费设置请求 DTO
 * 接口: PUT /api/admin/setting/freight
 */
@Data
public class FreightSettingDTO {

    /**
     * 默认运费(元)，最小 0
     * 0 表示全场免运费
     */
    @NotNull(message = "默认运费不能为空")
    @DecimalMin(value = "0", message = "运费不能为负数")
    private BigDecimal defaultFreight;

    /** 是否开启包邮功能 */
    @NotNull(message = "包邮开关不能为空")
    private Boolean freeFreightEnabled;

    /**
     * 满额包邮金额(元)
     * freeFreightEnabled=true 时生效，0 表示全场免运费
     */
    @DecimalMin(value = "0", message = "包邮金额不能为负数")
    private BigDecimal freeFreightAmount;
}
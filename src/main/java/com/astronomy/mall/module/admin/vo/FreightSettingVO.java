package com.astronomy.mall.module.admin.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 运费设置响应 VO
 * 接口: GET /api/admin/setting/freight
 */
@Data
public class FreightSettingVO {

    /** 默认运费(元) */
    private BigDecimal defaultFreight;

    /** 是否开启包邮 */
    private Boolean freeFreightEnabled;

    /** 满额包邮金额(元) */
    private BigDecimal freeFreightAmount;
}
package com.astronomy.mall.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址实体类
 * 对应数据库表: tb_address
 *
 * 📌 业务规则:
 * - 每个用户最多5个地址
 * - is_default=1 表示默认地址，同一用户只能有一个默认地址
 * - 地址删除不影响历史订单（订单里已有快照）
 */
@Data
@TableName("tb_address")
public class Address {

    /** 地址ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联 tb_user.id */
    private Long userId;

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
     * ⚠️ 同一用户只能有一条 is_default=1，setDefault时用事务保证原子性
     */
    private Integer isDefault;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
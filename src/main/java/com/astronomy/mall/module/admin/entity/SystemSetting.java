package com.astronomy.mall.module.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统设置实体类
 *
 * 📌 设计说明:
 *   采用 key-value 键值对存储，按 groupName 分组管理
 *   例如: group_name='basic', setting_key='mall_name', setting_value='天文器材商城'
 *
 * 📌 分组说明:
 *   basic       - 基础设置 (商城名称/Logo/联系方式)
 *   freight     - 运费设置 (默认运费/包邮金额)
 *   payment     - 支付设置 (支付方式开关/超时时间)
 *   seo         - SEO设置  (网站标题/关键词/描述)
 *   register    - 注册设置 (是否开放注册/邮箱验证)
 *   maintenance - 维护模式 (维护开关/提示语)
 */
@Data
@TableName("tb_system_setting")
public class SystemSetting {

    /** 配置ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置分组 */
    private String groupName;

    /** 配置键 */
    private String settingKey;

    /** 配置值 (字符串/数字/布尔/JSON) */
    private String settingValue;

    /** 值类型: string / number / boolean / json */
    private String valueType;

    /** 配置说明 */
    private String description;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
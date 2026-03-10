package com.astronomy.mall.module.aftersale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安装预约实体类
 * 对应数据库表: tb_installation
 *
 * 状态说明:
 *   0 - 待确认 (用户可取消)
 *   1 - 已确认 (等待工程师上门)
 *   2 - 已取消 (终态，用户取消或管理员取消)
 *
 * 📌 地址和联系人字段在提交预约时从订单自动快照，不可修改
 */
@Data
@TableName("tb_installation")
public class Installation {

    /** 预约ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID 关联tb_user.id */
    private Long userId;

    /** 关联订单ID 关联tb_order.id */
    private Long orderId;

    /** 关联商品ID 关联tb_product.id */
    private Long productId;

    /** 商品名称(快照) */
    private String productName;

    /** 安装地址(从订单自动带入: 省+市+区+详细地址拼接) */
    private String address;

    /** 联系人(从订单receiver_name快照) */
    private String contactName;

    /** 联系电话(从订单receiver_phone快照) */
    private String contactPhone;

    /** 期望上门时间(用户填写) */
    private LocalDateTime expectedTime;

    /** 确认上门时间(管理员确认时填写) */
    private LocalDateTime confirmedTime;

    /** 工程师姓名(管理员确认时填写) */
    private String engineerName;

    /** 工程师联系方式(管理员确认时填写) */
    private String engineerPhone;

    /** 用户备注 */
    private String userRemark;

    /** 管理员备注/取消原因 */
    private String adminRemark;

    /** 状态(0-待确认 1-已确认 2-已取消) */
    private Integer status;

    /** 操作管理员ID */
    private Long adminId;

    /** 预约提交时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
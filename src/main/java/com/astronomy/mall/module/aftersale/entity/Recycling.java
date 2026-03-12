package com.astronomy.mall.module.aftersale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手回收申请实体类
 *
 * 📌 对应数据库表: tb_recycling
 * 📌 状态说明:
 *   0 - 待审核   ✅ 用户可取消
 *   1 - 已报价   ✅ 等待用户确认/拒绝
 *   2 - 用户确认 ✅ 等待管理员安排取件
 *   3 - 待取件   ✅ 快递正在上门取件中
 *   4 - 已回收   ❌ 终态（余额已自动到账）
 *   5 - 已拒绝   ❌ 终态（管理员拒绝）
 *   6 - 用户取消 ❌ 终态
 */
@Data
@TableName("tb_recycling")
public class Recycling {

    /** 申请ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 回收单号（唯一） */
    private String recycleNo;

    /** 用户ID，关联 tb_user.id */
    private Long userId;

    /** 器材名称 */
    private String productName;

    /** 品牌（可选） */
    private String brand;

    /** 型号（可选） */
    private String model;

    /**
     * 成色等级
     * S - 全新/几乎未使用
     * A - 九成新，无明显磨损
     * B - 七八成新，有轻微使用痕迹
     * C - 六成以下，有明显使用痕迹或瑕疵
     */
    private String conditionLevel;

    /** 器材描述（问题/配件/使用情况等） */
    private String description;

    /** 管理员报价金额 */
    private BigDecimal assessedPrice;

    /** 管理员报价备注 / 拒绝原因 */
    private String adminRemark;

    /** 上门取件快递公司 */
    private String logisticsCompany;

    /** 取件快递单号 */
    private String trackingNumber;

    /**
     * 申请状态
     * 0-待审核 / 1-已报价 / 2-用户确认 / 3-待取件 / 4-已回收 / 5-已拒绝 / 6-用户取消
     */
    private Integer status;

    /** 用户确认时间 */
    private LocalDateTime confirmTime;

    /** 回收完成时间 */
    private LocalDateTime completeTime;

    /** 操作管理员ID */
    private Long adminId;
    /**
     * 器材实拍图片
     * 格式：JSON数组字符串，如 ["data:image/jpeg;base64,...", ...]
     * 最多6张，前端压缩至 1200px / JPEG 0.82 质量后存储
     */
    private String images;
    /** 申请创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最后更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
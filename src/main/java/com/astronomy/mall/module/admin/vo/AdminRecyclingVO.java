package com.astronomy.mall.module.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员端 - 回收申请 VO
 *
 * 📌 用于管理员列表和详情接口返回，包含用户信息
 */
@Data
public class AdminRecyclingVO {

    /** 申请ID */
    private Long id;

    /** 回收单号 */
    private String recycleNo;

    /** 申请用户ID */
    private Long userId;

    /** 申请用户名 */
    private String username;

    /** 申请用户昵称 */
    private String nickname;

    /** 用户手机号 */
    private String phone;

    /** 器材名称 */
    private String productName;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 成色等级 (S/A/B/C) */
    private String conditionLevel;

    /** 成色等级中文描述 */
    private String conditionLevelText;

    /** 器材描述 */
    private String description;

    /** 管理员报价金额 */
    private BigDecimal assessedPrice;

    /** 管理员备注（报价说明 / 拒绝原因） */
    private String adminRemark;

    /** 快递公司 */
    private String logisticsCompany;

    /** 快递单号 */
    private String trackingNumber;

    /**
     * 状态值
     * 0-待审核 1-已报价 2-用户确认 3-待取件 4-已回收 5-已拒绝 6-用户取消
     */
    private Integer status;

    /** 状态中文描述 */
    private String statusText;

    /** 操作管理员ID */
    private Long adminId;

    /** 操作管理员名称 */
    private String adminName;

    /** 器材实拍图片 JSON字符串 */
    private String images;
    /** 用户确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /** 回收完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
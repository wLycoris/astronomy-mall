package com.astronomy.mall.module.aftersale.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手回收申请 VO（用户端展示）
 *
 * 📌 用于用户申请列表、申请详情接口返回
 */
@Data
public class RecyclingVO {

    /** 申请ID */
    private Long id;

    /** 回收单号 */
    private String recycleNo;

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

    /** 快递公司（取件快递） */
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

    /** 器材实拍图片 JSON字符串，前端用 JSON.parse() 反序列化 */
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

    // ==================== 工具方法 ====================

    /**
     * 根据状态值返回中文描述
     */
    public static String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已报价";
            case 2: return "已确认";
            case 3: return "待取件";
            case 4: return "已回收";
            case 5: return "已拒绝";
            case 6: return "用户取消";
            default: return "未知";
        }
    }

    /**
     * 根据成色等级返回中文描述
     */
    public static String getConditionText(String level) {
        if (level == null) return "";
        switch (level) {
            case "S": return "全新/几乎未使用";
            case "A": return "九成新，无明显磨损";
            case "B": return "七八成新，有轻微使用痕迹";
            case "C": return "六成以下，有明显使用痕迹";
            default: return level;
        }
    }
}
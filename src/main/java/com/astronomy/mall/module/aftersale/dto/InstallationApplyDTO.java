package com.astronomy.mall.module.aftersale.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 提交安装预约 DTO
 *
 * 📌 地址和联系人无需前端传入，后端从订单自动快照
 * 📌 后端校验规则:
 *   1. order_id 必须属于当前登录用户 (防越权)
 *   2. 订单状态必须是 status=2(待收货) 或 status=3(已完成)
 *   3. 同一订单不能重复提交预约
 *   4. product_id 必须是该订单内的商品
 */
@Data
public class InstallationApplyDTO {

    /**
     * 关联订单ID（必填）
     * 后端从此订单中提取地址和联系人信息
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 关联商品ID（必填）
     * 必须是该订单中包含的商品
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 期望上门时间（选填）
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedTime;

    /**
     * 用户备注（选填）
     * 如: 门禁密码、停车注意事项等
     */
    private String userRemark;
}
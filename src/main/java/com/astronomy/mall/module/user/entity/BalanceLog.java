package com.astronomy.mall.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额流水记录实体
 * 对应数据库表: tb_balance_log
 *
 * 流水类型:
 *   1 - 充值   (amount > 0)
 *   2 - 提现   (amount < 0)
 *   3 - 回收入账 (amount > 0)
 *   4 - 购物扣款 (amount < 0)
 *
 * ⚠️ 只读，所有写入必须通过 BalanceService.changeBalance()
 *
 * 文件路径: com.astronomy.mall.module.user.entity.BalanceLog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_balance_log")
public class BalanceLog {

    /** 流水ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联 tb_user.id */
    private Long userId;

    /**
     * 流水类型
     * 1-充值  2-提现  3-回收入账  4-购物扣款
     */
    private Integer type;

    /**
     * 变动金额
     * 正数=收入，负数=支出
     */
    private BigDecimal amount;

    /** 变动前余额快照 */
    private BigDecimal balanceBefore;

    /** 变动后余额快照 */
    private BigDecimal balanceAfter;

    /** 备注，如订单号、回收单号等 */
    private String remark;

    /**
     * 关联业务ID
     * 充值/提现时为 null，订单扣款时为 orderId，回收入账时为 recyclingId
     */
    private Long relatedId;

    /**
     * 关联业务类型
     * order / recycling / recharge / withdraw
     */
    private String relatedType;

    /** 创建时间（流水时间） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
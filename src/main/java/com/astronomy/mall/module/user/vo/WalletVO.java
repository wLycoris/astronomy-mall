package com.astronomy.mall.module.user.vo;

import com.astronomy.mall.module.user.entity.BalanceLog;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包首页 VO
 * 包含: 当前余额 + 最近20条流水
 *
 * 文件路径: com.astronomy.mall.module.user.vo.WalletVO
 */
@Data
public class WalletVO {

    /** 当前余额 */
    private BigDecimal balance;

    /**
     * 最近20条流水记录
     * 详情见 BalanceLog 实体
     */
    private List<BalanceLog> logs;
}
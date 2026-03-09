package com.astronomy.mall.module.user.service;

import com.astronomy.mall.module.user.entity.BalanceLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;

/**
 * 余额统一服务接口
 *
 * ⚠️ 所有余额变动必须调用 changeBalance()，严禁直接 UPDATE tb_user SET balance=xxx
 *
 * 调用方：
 *   - WalletController（充值/提现）
 *   - OrderServiceImpl.createOrder()（余额全额抵扣）
 *   - PaymentServiceImpl（payment_type=3 余额支付 或 部分余额抵扣）
 *   - AdminRecyclingServiceImpl（回收完成入账）—— 二手回收模块开发时接入
 *
 * 文件路径: com.astronomy.mall.module.user.service.BalanceService
 */
public interface BalanceService {

    /**
     * 余额变动统一入口（含行锁防并发）
     *
     * @param userId      用户ID
     * @param amount      变动金额（正数=收入，负数=支出）
     * @param type        流水类型（1-充值 2-提现 3-回收入账 4-购物扣款）
     * @param remark      备注（如订单号、回收单号）
     * @param relatedId   关联业务ID（可为 null）
     * @param relatedType 关联业务类型（order / recycling / recharge / withdraw，可为 null）
     * @throws com.astronomy.mall.common.exception.BusinessException 余额不足时抛出
     */
    void changeBalance(Long userId, BigDecimal amount, Integer type,
                       String remark, Long relatedId, String relatedType);

    /**
     * 查询用户当前余额
     *
     * @param userId 用户ID
     * @return 当前余额
     */
    BigDecimal getBalance(Long userId);

    /**
     * 分页查询余额流水（用于流水列表页）
     *
     * @param userId   用户ID
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页流水列表
     */
    IPage<BalanceLog> getBalanceLogPage(Long userId, Integer pageNum, Integer pageSize);
}
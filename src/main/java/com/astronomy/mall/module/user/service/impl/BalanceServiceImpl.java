package com.astronomy.mall.module.user.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.user.entity.BalanceLog;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.BalanceLogMapper;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.astronomy.mall.module.user.service.BalanceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 余额统一服务实现
 *
 * 核心安全机制：
 * 1. SELECT ... FOR UPDATE 行级锁，防止并发扣款导致余额超支
 * 2. 余额变动与流水记录在同一事务，保证强一致性
 * 3. 余额不足立即抛 BusinessException，事务自动回滚
 *
 * ⚠️ 所有余额变动必须通过此类的 changeBalance() 方法
 *
 * 文件路径: com.astronomy.mall.module.user.service.impl.BalanceServiceImpl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    /** 用户 Mapper（需新增 selectByIdForUpdate + updateBalance 方法） */
    private final UserMapper userMapper;

    /** 余额流水 Mapper */
    private final BalanceLogMapper balanceLogMapper;

    // ============================================================
    // 核心方法：余额变动统一入口
    // ============================================================

    /**
     * 余额变动统一入口
     *
     * 执行步骤：
     * 1. SELECT ... FOR UPDATE 加行锁（由 UserMapper.selectByIdForUpdate 实现）
     * 2. 计算变动后余额
     * 3. 校验余额不能为负
     * 4. 更新 tb_user.balance
     * 5. 同事务插入 tb_balance_log 流水
     *
     * @param userId      用户ID
     * @param amount      变动金额（正=收入，负=支出）
     * @param type        1-充值 2-提现 3-回收入账 4-购物扣款
     * @param remark      备注
     * @param relatedId   关联ID
     * @param relatedType 关联类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeBalance(Long userId, BigDecimal amount, Integer type,
                              String remark, Long relatedId, String relatedType) {

        // ── Step 1: 行锁查询用户，防止并发扣款超支 ──────────────────────
        User user = userMapper.selectByIdForUpdate(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        BigDecimal before = user.getBalance();
        BigDecimal after  = before.add(amount);

        // ── Step 2: 余额不能为负 ─────────────────────────────────────────
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("[钱包] 用户{}余额不足: 当前={}, 扣款={}", userId, before, amount.abs());
            throw new BusinessException("余额不足，当前余额：¥" + before.toPlainString());
        }

        // ── Step 3: 更新余额 ─────────────────────────────────────────────
        userMapper.updateBalance(userId, after);
        log.info("[钱包] 用户{}余额变动: {}→{}, type={}, remark={}", userId, before, after, type, remark);

        // ── Step 4: 同事务插入流水（与余额更新保证原子性） ───────────────
        BalanceLog log_entry = BalanceLog.builder()
                .userId(userId)
                .type(type)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .remark(remark)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .build();
        balanceLogMapper.insert(log_entry);
    }

    // ============================================================
    // 查询方法
    // ============================================================

    @Override
    public BigDecimal getBalance(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return user.getBalance();
    }

    @Override
    public IPage<BalanceLog> getBalanceLogPage(Long userId, Integer pageNum, Integer pageSize) {
        Page<BalanceLog> page = new Page<>(pageNum, pageSize);
        return balanceLogMapper.selectPageByUserId(page, userId);
    }
}
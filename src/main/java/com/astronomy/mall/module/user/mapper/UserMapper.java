package com.astronomy.mall.module.user.mapper;

import com.astronomy.mall.module.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * MyBatis-Plus已提供以下方法:
     * - insert(User)
     * - deleteById(Serializable)
     * - updateById(User)
     * - selectById(Serializable)
     * - selectOne(Wrapper)
     * - selectList(Wrapper)
     * - selectPage(Page, Wrapper)
     *
     * 如需自定义SQL,在UserMapper.xml中编写
     */

    // ── 2.4.4 钱包系统 新增 ──────────────────────────────────────────

    /**
     * 行锁查询用户（SELECT ... FOR UPDATE）
     * ⚠️ 必须在 @Transactional 方法内调用，仅供 BalanceService.changeBalance() 使用
     *
     * @param userId 用户ID
     * @return 用户对象（含 balance 字段）
     */
    User selectByIdForUpdate(@Param("userId") Long userId);

    /**
     * 直接更新用户余额
     * ⚠️ 仅供 BalanceService.changeBalance() 在同一事务中调用
     *    严禁其他地方直接调用，余额变动必须通过 BalanceService.changeBalance()
     *
     * @param userId     用户ID
     * @param balance    新余额（已在 BalanceService 中校验不为负）
     */
    void updateBalance(@Param("userId") Long userId, @Param("balance") BigDecimal balance);
}
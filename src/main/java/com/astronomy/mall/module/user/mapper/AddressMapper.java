package com.astronomy.mall.module.user.mapper;

import com.astronomy.mall.module.user.entity.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址 Mapper
 *
 * 📌 核心方法说明:
 * - clearDefault: 清除某用户所有默认地址（set is_default=0）
 * - setDefault:   将指定地址设为默认（set is_default=1）
 * - 两个方法必须在同一事务中调用，由 AddressServiceImpl.setDefault() 保证
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /**
     * 查询用户的收货地址列表（按 is_default DESC, id ASC 排序，默认地址置顶）
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计某用户当前地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 清除用户所有地址的默认标记（is_default → 0）
     * ⚠️ 设置默认地址前必须先调用此方法，与 setDefault 在同一事务中
     *
     * @param userId 用户ID
     */
    void clearDefault(@Param("userId") Long userId);

    /**
     * 将指定地址设为默认（is_default → 1）
     * ⚠️ 必须与 clearDefault 在同一事务中，防止出现多个默认地址
     *
     * @param userId    用户ID（防止越权）
     * @param addressId 地址ID
     */
    void setDefault(@Param("userId") Long userId, @Param("addressId") Long addressId);
}
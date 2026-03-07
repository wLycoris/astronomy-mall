package com.astronomy.mall.module.user.service;

import com.astronomy.mall.module.user.dto.AddressDTO;
import com.astronomy.mall.module.user.entity.Address;
import com.astronomy.mall.module.user.vo.AddressVO;

import java.util.List;

/**
 * 收货地址 Service 接口
 *
 * 📌 接口清单 (5个):
 * 1. getAddressList  - 获取我的地址列表（默认地址置顶）
 * 2. addAddress      - 新增地址（最多5个）
 * 3. updateAddress   - 编辑地址
 * 4. deleteAddress   - 删除地址（不影响历史订单快照）
 * 5. setDefaultAddress - 设为默认地址（事务保证唯一性）
 *
 * 📌 额外工具方法:
 * 6. getAddressById  - 供 OrderServiceImpl 下单时查询地址快照
 */
public interface AddressService {

    /**
     * 获取当前用户的收货地址列表
     * 默认地址排在最前面
     *
     * @param userId 当前用户ID
     * @return 地址列表（最多5条）
     */
    List<AddressVO> getAddressList(Long userId);

    /**
     * 新增收货地址
     * - 同一用户最多5个地址，超出则抛出异常
     * - 若 isDefault=1，自动清除其他默认标记
     * - 若是该用户第一个地址，自动设为默认
     *
     * @param userId     当前用户ID
     * @param addressDTO 地址信息
     */
    void addAddress(Long userId, AddressDTO addressDTO);

    /**
     * 编辑收货地址
     * - 校验地址归属（防止修改他人地址）
     * - 若 isDefault=1，自动清除其他默认标记
     *
     * @param userId     当前用户ID
     * @param addressId  地址ID
     * @param addressDTO 新的地址信息
     */
    void updateAddress(Long userId, Long addressId, AddressDTO addressDTO);

    /**
     * 删除收货地址
     * - 校验地址归属（防止删除他人地址）
     * - 删除默认地址时，自动将最新的其他地址设为默认
     * - 订单中的收货信息为快照，删除地址不影响历史订单
     *
     * @param userId    当前用户ID
     * @param addressId 地址ID
     */
    void deleteAddress(Long userId, Long addressId);

    /**
     * 设为默认收货地址
     * - 事务保证：先清除该用户所有默认标记，再设置目标地址
     * - 校验地址归属（防止操作他人地址）
     *
     * @param userId    当前用户ID
     * @param addressId 地址ID
     */
    void setDefaultAddress(Long userId, Long addressId);

    /**
     * 根据ID查询地址（供下单时快照使用）
     * - 校验地址归属
     *
     * @param userId    当前用户ID（用于校验归属）
     * @param addressId 地址ID
     * @return Address 实体（含完整地址信息，用于快照到订单）
     */
    Address getAddressById(Long userId, Long addressId);
}
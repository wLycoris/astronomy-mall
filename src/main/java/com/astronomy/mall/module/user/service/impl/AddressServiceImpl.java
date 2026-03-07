package com.astronomy.mall.module.user.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.user.dto.AddressDTO;
import com.astronomy.mall.module.user.entity.Address;
import com.astronomy.mall.module.user.mapper.AddressMapper;
import com.astronomy.mall.module.user.service.AddressService;
import com.astronomy.mall.module.user.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收货地址 Service 实现
 *
 * 📌 核心业务规则:
 * 1. 每个用户最多 5 个收货地址
 * 2. 同一用户只能有一个默认地址（用事务保证原子性）
 * 3. 第一个地址自动设为默认
 * 4. 删除默认地址时，自动将最旧的其他地址设为新默认（若还有其他地址）
 * 5. 地址被删除不影响历史订单（订单存的是快照字段）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    /** 每个用户最多可保存的地址数量 */
    private static final int MAX_ADDRESS_COUNT = 5;

    private final AddressMapper addressMapper;

    // =====================================================================
    // 查询地址列表
    // =====================================================================

    @Override
    public List<AddressVO> getAddressList(Long userId) {
        List<Address> addresses = addressMapper.selectByUserId(userId);
        return addresses.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // 新增地址
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long userId, AddressDTO dto) {
        // 1. 校验地址数量上限
        int count = addressMapper.countByUserId(userId);
        if (count >= MAX_ADDRESS_COUNT) {
            throw new BusinessException("收货地址最多保存 " + MAX_ADDRESS_COUNT + " 个");
        }

        // 2. 构建地址实体
        Address address = new Address();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);

        // 3. 设为默认逻辑：
        //    - 若是该用户第一个地址，强制设为默认（无论 isDefault 传什么）
        //    - 若 isDefault=1 且已有其他地址，需要先清除旧默认
        boolean isFirstAddress = (count == 0);
        if (isFirstAddress) {
            address.setIsDefault(1);
        } else if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            // 先清除旧默认，再新增时 is_default=1
            addressMapper.clearDefault(userId);
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        // 4. 插入数据库
        addressMapper.insert(address);
        log.info("[AddressService] 用户 {} 新增地址 {}", userId, address.getId());
    }

    // =====================================================================
    // 编辑地址
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Long addressId, AddressDTO dto) {
        // 1. 查询并校验归属
        Address address = getAndCheckOwner(userId, addressId);

        // 2. 更新字段
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());

        // 3. 处理默认地址逻辑
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            // 想要设为默认：先清除其他，再设置当前
            addressMapper.clearDefault(userId);
            address.setIsDefault(1);
        } else if (Integer.valueOf(0).equals(dto.getIsDefault())) {
            // 明确传 0 且当前是默认地址 → 不允许取消默认（至少保留一个默认）
            if (Integer.valueOf(1).equals(address.getIsDefault())) {
                // 保持原状，不变更默认标记
                address.setIsDefault(1);
            } else {
                address.setIsDefault(0);
            }
        }
        // isDefault 为 null 时，保持原状（不做变更）

        // 4. 更新数据库
        addressMapper.updateById(address);
        log.info("[AddressService] 用户 {} 编辑地址 {}", userId, addressId);
    }

    // =====================================================================
    // 删除地址
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long addressId) {
        // 1. 查询并校验归属
        Address address = getAndCheckOwner(userId, addressId);

        // 2. 物理删除
        addressMapper.deleteById(addressId);
        log.info("[AddressService] 用户 {} 删除地址 {}", userId, addressId);

        // 3. 若删除的是默认地址，将剩余最旧的地址设为新默认
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            List<Address> remaining = addressMapper.selectByUserId(userId);
            if (!remaining.isEmpty()) {
                // remaining 已按 id ASC 排序，取第一条（最早添加的）设为默认
                Address newDefault = remaining.get(remaining.size() - 1);
                // 实际上 selectByUserId 已经按 is_default DESC, id ASC 排序
                // 删除默认后，取第一条（id 最小的）设为新默认
                Address oldest = remaining.get(0);
                oldest.setIsDefault(1);
                addressMapper.updateById(oldest);
                log.info("[AddressService] 默认地址被删除，自动将地址 {} 设为新默认", oldest.getId());
            }
        }
    }

    // =====================================================================
    // 设为默认地址
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long addressId) {
        // 1. 校验地址归属
        getAndCheckOwner(userId, addressId);

        // 2. 先清除该用户所有默认标记
        addressMapper.clearDefault(userId);

        // 3. 再将目标地址设为默认
        addressMapper.setDefault(userId, addressId);

        log.info("[AddressService] 用户 {} 将地址 {} 设为默认", userId, addressId);
    }

    // =====================================================================
    // 根据ID查询地址（供下单快照使用）
    // =====================================================================

    @Override
    public Address getAddressById(Long userId, Long addressId) {
        return getAndCheckOwner(userId, addressId);
    }

    // =====================================================================
    // 私有工具方法
    // =====================================================================

    /**
     * 查询地址并校验所有权
     * - 地址不存在 → 抛异常
     * - 地址不属于当前用户 → 抛异常（防越权）
     */
    private Address getAndCheckOwner(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException("无权操作该地址");
        }
        return address;
    }

    /**
     * Address 实体 → AddressVO
     */
    private AddressVO convertToVO(Address address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }
}
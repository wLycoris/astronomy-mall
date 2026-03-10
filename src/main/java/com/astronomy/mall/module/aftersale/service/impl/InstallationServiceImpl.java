package com.astronomy.mall.module.aftersale.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.aftersale.dto.InstallationApplyDTO;
import com.astronomy.mall.module.aftersale.entity.Installation;
import com.astronomy.mall.module.aftersale.mapper.InstallationMapper;
import com.astronomy.mall.module.aftersale.service.InstallationService;
import com.astronomy.mall.module.aftersale.vo.InstallationVO;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.entity.OrderItem;
import com.astronomy.mall.module.order.mapper.OrderItemMapper;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 安装预约 ServiceImpl（用户端）
 *
 * 📌 文件路径:
 *   module/aftersale/service/impl/InstallationServiceImpl.java
 *
 * 📌 依赖注入说明:
 *   - OrderMapper: 查询订单信息（校验归属、获取地址）
 *   - OrderItemMapper: 查询订单商品（校验productId + 获取商品名快照）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstallationServiceImpl
        extends ServiceImpl<InstallationMapper, Installation>
        implements InstallationService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    // ─────────────────────────────────────────────
    // 提交安装预约
    // ─────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitInstallation(Long userId, InstallationApplyDTO dto) {

        // ① 查询订单，校验存在性
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }

        // ② 校验订单归属（防越权访问）
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此订单");
        }

        // ③ 校验订单状态：必须是 2(待收货) 或 3(已完成)
        //    待支付/待发货的订单还未到手，不允许预约安装
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "只有「待收货」或「已完成」的订单才能预约安装服务");

        }

        // ④ 校验同一订单不能重复提交预约（UNIQUE KEY uk_order_id 也会兜底）
        long exists = lambdaQuery()
                .eq(Installation::getOrderId, dto.getOrderId())
                .count();
        if (exists > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该订单已提交过安装预约");

        }

        // ⑤ 校验 productId 是否属于该订单，并获取商品名称快照
        OrderItem orderItem = orderItemMapper.selectOne(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, dto.getOrderId())
                        .eq(OrderItem::getProductId, dto.getProductId())
        );
        if (orderItem == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "所选商品不在此订单中");
        }

        // ⑥ 构建安装预约记录，从订单自动快照地址和联系人
        Installation installation = new Installation();
        installation.setUserId(userId);
        installation.setOrderId(dto.getOrderId());
        installation.setProductId(dto.getProductId());
        installation.setProductName(orderItem.getProductName());

        // 地址拼接: 省 + 市 + 区 + 详细地址
        String address = nullToEmpty(order.getReceiverProvince())
                + nullToEmpty(order.getReceiverCity())
                + nullToEmpty(order.getReceiverDistrict())
                + nullToEmpty(order.getReceiverAddress());
        installation.setAddress(address);
        installation.setContactName(order.getReceiverName());
        installation.setContactPhone(order.getReceiverPhone());

        installation.setExpectedTime(dto.getExpectedTime());
        installation.setUserRemark(dto.getUserRemark());
        installation.setStatus(0); // 待确认

        save(installation);
        log.info("安装预约提交成功: userId={}, orderId={}, installationId={}",
                userId, dto.getOrderId(), installation.getId());
    }

    // ─────────────────────────────────────────────
    // 查询我的预约列表
    // ─────────────────────────────────────────────

    @Override
    public IPage<InstallationVO> getMyList(Long userId, Integer pageNum, Integer pageSize) {
        IPage<InstallationVO> page = new Page<>(
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize
        );
        return baseMapper.selectUserList(page, userId);
    }

    // ─────────────────────────────────────────────
    // 用户取消预约
    // ─────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInstallation(Long userId, Long id) {
        // ① 查询预约记录
        Installation installation = getById(id);
        if (installation == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "预约记录不存在");
        }

        // ② 校验归属（防越权）
        if (!installation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此预约");
        }

        // ③ 只允许取消「待确认」状态的预约
        if (installation.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "只有待确认的预约才能取消");

        }

        // ④ 更新状态为已取消
        Installation update = new Installation();
        update.setId(id);
        update.setStatus(2); // 已取消
        updateById(update);

        log.info("用户取消安装预约: userId={}, installationId={}", userId, id);
    }

    // ─────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────

    private String nullToEmpty(String str) {
        return str == null ? "" : str;
    }
}
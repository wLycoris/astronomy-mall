package com.astronomy.mall.module.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.cart.entity.Cart;
import com.astronomy.mall.module.cart.mapper.CartMapper;
import com.astronomy.mall.module.cart.vo.CartVO;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.order.dto.CreateOrderDTO;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.entity.OrderItem;
import com.astronomy.mall.module.order.mapper.OrderItemMapper;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.order.service.OrderService;
import com.astronomy.mall.module.order.vo.OrderItemVO;
import com.astronomy.mall.module.order.vo.OrderVO;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.astronomy.mall.module.admin.entity.SystemSetting;
import com.astronomy.mall.module.admin.mapper.SystemSettingMapper;
import com.astronomy.mall.module.user.entity.Address;
import com.astronomy.mall.module.user.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单 Service 实现
 *
 * 📌 v7.6 改造说明 (2.4.2 收货地址管理):
 *   createOrder() 方法收货信息获取方式变更：
 *   - 改造前: 从 DTO 直接读取 receiverName/receiverPhone/省市区/address 字段
 *   - 改造后: 从 DTO 读取 addressId，调用 AddressService.getAddressById() 查询地址
 *             再将地址各字段快照到 Order 实体，防止用户后续删地址影响历史订单
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper          orderMapper;
    private final OrderItemMapper      orderItemMapper;
    private final CartMapper           cartMapper;
    private final ProductMapper        productMapper;
    private final SystemSettingMapper  systemSettingMapper;

    // 📌 v7.6 新增注入：地址服务，用于下单时查询并快照地址
    private final AddressService addressService;

    @Autowired
    private NotificationHelper notificationHelper;

    // =====================================================================
    // 创建订单
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, CreateOrderDTO dto) {

        // ── Step 1: 查询选中的购物车项 ───────────────────────────────────
        List<CartVO> cartItems = cartMapper.selectSelectedCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(2201, "请选择要购买的商品");
        }

        // ── Step 2: 按传入的 cartIds 过滤（若传了的话）────────────────────
        if (dto.getCartIds() != null && !dto.getCartIds().isEmpty()) {
            List<Long> cartIds = dto.getCartIds();
            cartItems = cartItems.stream()
                    .filter(item -> cartIds.contains(item.getId()))
                    .collect(Collectors.toList());
        }

        // ── Step 3: 校验库存 + 计算商品总金额 ───────────────────────────
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartVO cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null) {
                throw new BusinessException(2001, "商品[" + cartItem.getProductName() + "]不存在");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(2002, "商品[" + product.getProductName() + "]库存不足");
            }
            totalAmount = totalAmount.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        // ── Step 4: 动态计算运费（从系统设置读取）─────────────────────────
        BigDecimal freight = calcFreight(totalAmount);

        // ── Step 5: 查询收货地址并快照 ──────────────────────────────────
        Address address = addressService.getAddressById(userId, dto.getAddressId());

        // ── Step 6: 构建订单实体 ─────────────────────────────────────────
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);

        // 收货信息快照
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getDetail());

        // 价格信息
        order.setTotalAmount(totalAmount);
        order.setFreight(freight);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaymentAmount(totalAmount.add(freight));

        order.setStatus(0);  // 待支付
        order.setRemark(dto.getRemark());

        orderMapper.insert(order);

        // ── Step 7: 创建订单详情 + 扣减库存 ─────────────────────────────
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartVO cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setProductBrand(product.getBrand());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );

            orderItems.add(orderItem);
            orderItemMapper.insert(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productMapper.updateById(product);
        }

        // ── Step 8: 删除购物车中的已购买商品 ────────────────────────────
        List<Long> purchasedCartIds = cartItems.stream()
                .map(CartVO::getId)
                .collect(Collectors.toList());
        if (!purchasedCartIds.isEmpty()) {
            LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Cart::getId, purchasedCartIds);
            cartMapper.delete(wrapper);
        }

        // ── Step 9: 返回订单 VO ──────────────────────────────────────────
        return convertToVO(order, orderItems);
    }

    // =====================================================================
    // 动态计算运费（从系统设置读取规则）
    // =====================================================================

    private BigDecimal calcFreight(BigDecimal totalAmount) {
        try {
            List<SystemSetting> settings = systemSettingMapper.selectByGroupName("freight");
            Map<String, String> fs = new HashMap<>();
            settings.forEach(s -> fs.put(s.getSettingKey(), s.getSettingValue()));

            BigDecimal defaultFreight     = new BigDecimal(fs.getOrDefault("default_freight",       "0"));
            boolean    freeFreightEnabled = Boolean.parseBoolean(fs.getOrDefault("free_freight_enabled", "false"));
            BigDecimal freeFreightAmount  = new BigDecimal(fs.getOrDefault("free_freight_amount",    "0"));

            if (defaultFreight.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            if (freeFreightEnabled && totalAmount.compareTo(freeFreightAmount) >= 0) {
                return BigDecimal.ZERO;
            }
            return defaultFreight;

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // =====================================================================
    // 订单列表
    // =====================================================================

    @Override
    public Page<OrderVO> getOrderList(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<OrderVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    // =====================================================================
    // 订单详情
    // =====================================================================

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return convertToVO(order, orderItemMapper.selectList(wrapper));
    }

    // =====================================================================
    // 取消订单
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(3002, "只能取消待支付的订单");
        }

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        order.setStatus(4);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderMapper.updateById(order);

        notificationHelper.sendOrderCancelledNotification(
                order.getUserId(), order.getOrderNo(), order.getId()
        );
    }

    // =====================================================================
    // 确认收货
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(3003, "只能确认待收货的订单");
        }

        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        order.setLogisticsStatus(3);
        orderMapper.updateById(order);

        notificationHelper.sendOrderCompletedNotification(
                order.getUserId(), order.getOrderNo(), order.getId()
        );
    }

    // =====================================================================
    // 删除订单
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }
        if (order.getStatus() != 3 && order.getStatus() != 4) {
            throw new BusinessException(3004, "只能删除已取消或已完成的订单");
        }
        orderMapper.deleteById(orderId);
    }

    // =====================================================================
    // 私有工具方法
    // =====================================================================

    private String generateOrderNo() {
        String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String randomNum = String.valueOf((int) (Math.random() * 10000));
        return "ORD" + timestamp + randomNum;
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);
        vo.setFullAddress(buildFullAddress(order));
        vo.setStatusText(getStatusText(order.getStatus()));

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        vo.setItems(orderItemMapper.selectList(wrapper).stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList()));
        return vo;
    }

    private OrderVO convertToVO(Order order, List<OrderItem> orderItems) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);
        vo.setFullAddress(buildFullAddress(order));
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setItems(orderItems.stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList()));
        return vo;
    }

    private String buildFullAddress(Order order) {
        return order.getReceiverProvince() + " "
                + order.getReceiverCity()     + " "
                + order.getReceiverDistrict() + " "
                + order.getReceiverAddress();
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }
}
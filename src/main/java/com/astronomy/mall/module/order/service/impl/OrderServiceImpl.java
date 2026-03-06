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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final SystemSettingMapper systemSettingMapper;  // 🆕 运费设置

    @Autowired
    private NotificationHelper notificationHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, CreateOrderDTO dto) {
        // 1. 查询选中的购物车项
        List<CartVO> cartItems = cartMapper.selectSelectedCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(2201, "请选择要购买的商品");
        }

        // 2. 过滤出指定的购物车项(如果传了cartIds)
        if (dto.getCartIds() != null && !dto.getCartIds().isEmpty()) {
            List<Long> cartIds = dto.getCartIds();
            cartItems = cartItems.stream()
                    .filter(item -> cartIds.contains(item.getId()))
                    .collect(Collectors.toList());
        }

        // 3. 检查库存并计算商品总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartVO cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null) {
                throw new BusinessException(2001, "商品[" + cartItem.getProductName() + "]不存在");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(2002, "商品[" + product.getProductName() + "]库存不足");
            }
            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        // 🆕 4. 从系统设置读取运费规则，动态计算运费
        BigDecimal freight = calcFreight(totalAmount);

        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);

        // 收货信息
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverProvince(dto.getReceiverProvince());
        order.setReceiverCity(dto.getReceiverCity());
        order.setReceiverDistrict(dto.getReceiverDistrict());
        order.setReceiverAddress(dto.getReceiverAddress());

        // 价格信息
        order.setTotalAmount(totalAmount);
        order.setFreight(freight);                                    // 🆕 动态运费
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaymentAmount(totalAmount.add(freight));             // 🆕 应付 = 商品总额 + 运费

        // 订单状态
        order.setStatus(0); // 待支付
        order.setRemark(dto.getRemark());

        orderMapper.insert(order);

        // 6. 创建订单详情 + 扣减库存
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
            BigDecimal totalPrice = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setTotalPrice(totalPrice);

            orderItems.add(orderItem);
            orderItemMapper.insert(orderItem);

            // 扣减库存
            product.setStock(product.getStock() - cartItem.getQuantity());
            productMapper.updateById(product);
        }

        // 7. 删除购物车中的已购买商品
        List<Long> cartIds = cartItems.stream()
                .map(CartVO::getId)
                .collect(Collectors.toList());
        if (!cartIds.isEmpty()) {
            LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Cart::getId, cartIds);
            cartMapper.delete(wrapper);
        }

        // 8. 返回订单VO
        return convertToVO(order, orderItems);
    }

    /**
     * 🆕 根据系统设置动态计算运费
     *
     * 规则：
     *  1. defaultFreight == 0                          → 全场免运费
     *  2. freeFreightEnabled && totalAmount >= freeFreightAmount → 满额包邮，运费 = 0
     *  3. 其他情况                                     → 收取 defaultFreight
     */
    private BigDecimal calcFreight(BigDecimal totalAmount) {
        try {
            List<SystemSetting> settings = systemSettingMapper.selectByGroupName("freight");
            Map<String, String> fs = new HashMap<>();
            settings.forEach(s -> fs.put(s.getSettingKey(), s.getSettingValue()));

            BigDecimal defaultFreight     = new BigDecimal(fs.getOrDefault("default_freight",      "0"));
            boolean    freeFreightEnabled = Boolean.parseBoolean(fs.getOrDefault("free_freight_enabled", "false"));
            BigDecimal freeFreightAmount  = new BigDecimal(fs.getOrDefault("free_freight_amount",   "0"));

            // 全场免运费
            if (defaultFreight.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            // 满额包邮
            if (freeFreightEnabled && totalAmount.compareTo(freeFreightAmount) >= 0) {
                return BigDecimal.ZERO;
            }
            return defaultFreight;

        } catch (Exception e) {
            // 读取设置失败时兜底返回 0，不影响下单流程
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Page<OrderVO> getOrderList(Long userId, Integer status, Integer pageNum, Integer pageSize) {

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        Page<OrderVO> voPage = new Page<>(pageNum, pageSize, orderPage.getTotal());
        List<OrderVO> voList = orderPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(3001, "订单不存在");
        }

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);

        return convertToVO(order, orderItems);
    }

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

        // 恢复库存
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
                order.getUserId(),
                order.getOrderNo(),
                order.getId()
        );
    }

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
                order.getUserId(),
                order.getOrderNo(),
                order.getId()
        );
    }

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

    private String generateOrderNo() {
        String timestamp = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        String randomNum = String.valueOf((int) (Math.random() * 10000));
        return "ORD" + timestamp + randomNum;
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);

        vo.setFullAddress(order.getReceiverProvince() + " " +
                order.getReceiverCity() + " " +
                order.getReceiverDistrict() + " " +
                order.getReceiverAddress());

        vo.setStatusText(getStatusText(order.getStatus()));

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);

        List<OrderItemVO> itemVOList = orderItems.stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList());
        vo.setItems(itemVOList);

        return vo;
    }

    private OrderVO convertToVO(Order order, List<OrderItem> orderItems) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);

        vo.setFullAddress(order.getReceiverProvince() + " " +
                order.getReceiverCity() + " " +
                order.getReceiverDistrict() + " " +
                order.getReceiverAddress());

        vo.setStatusText(getStatusText(order.getStatus()));

        List<OrderItemVO> itemVOList = orderItems.stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList());
        vo.setItems(itemVOList);

        return vo;
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
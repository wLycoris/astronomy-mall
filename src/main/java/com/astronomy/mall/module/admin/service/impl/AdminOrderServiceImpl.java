package com.astronomy.mall.module.admin.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.service.AdminOrderService;
import com.astronomy.mall.module.admin.vo.AdminOrderVO;
import com.astronomy.mall.module.admin.vo.OrderExportVO;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.order.entity.Order;
import com.astronomy.mall.module.order.entity.OrderItem;
import com.astronomy.mall.module.order.mapper.OrderItemMapper;
import com.astronomy.mall.module.order.mapper.OrderMapper;
import com.astronomy.mall.module.order.vo.OrderItemVO;
import com.astronomy.mall.module.payment.entity.Payment;
import com.astronomy.mall.module.payment.mapper.PaymentMapper;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员订单ServiceImpl
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Slf4j
@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    // 🔥 新增：注入通知助手
    @Autowired
    private NotificationHelper notificationHelper;

    /**
     * 订单状态映射
     */
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    /**
     * 物流状态映射
     */
    private static final Map<Integer, String> LOGISTICS_STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(0, "待支付");
        STATUS_MAP.put(1, "待发货");
        STATUS_MAP.put(2, "待收货");
        STATUS_MAP.put(3, "已完成");
        STATUS_MAP.put(4, "已取消");

        LOGISTICS_STATUS_MAP.put(0, "未发货");
        LOGISTICS_STATUS_MAP.put(1, "运输中");
        LOGISTICS_STATUS_MAP.put(2, "派送中");
        LOGISTICS_STATUS_MAP.put(3, "已签收");
    }

    @Override
    public Page<AdminOrderVO> getOrderList(OrderQueryDTO dto) {
        log.info("=== 管理员查询订单列表 ===");
        log.info("查询条件: {}", dto);

        // 构建查询条件
        Page<Order> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 订单编号
        if (StrUtil.isNotBlank(dto.getOrderNo())) {
            wrapper.like(Order::getOrderNo, dto.getOrderNo());
        }

        // 用户ID
        if (dto.getUserId() != null) {
            wrapper.eq(Order::getUserId, dto.getUserId());
        }

        // 收货人姓名
        if (StrUtil.isNotBlank(dto.getReceiverName())) {
            wrapper.like(Order::getReceiverName, dto.getReceiverName());
        }

        // 收货人电话
        if (StrUtil.isNotBlank(dto.getReceiverPhone())) {
            wrapper.like(Order::getReceiverPhone, dto.getReceiverPhone());
        }

        // 订单状态
        if (dto.getStatus() != null) {
            wrapper.eq(Order::getStatus, dto.getStatus());
        }

        // 物流状态
        if (dto.getLogisticsStatus() != null) {
            wrapper.eq(Order::getLogisticsStatus, dto.getLogisticsStatus());
        }

        // 时间范围
        if (StrUtil.isNotBlank(dto.getStartTime())) {
            wrapper.ge(Order::getCreateTime, dto.getStartTime());
        }
        if (StrUtil.isNotBlank(dto.getEndTime())) {
            wrapper.le(Order::getCreateTime, dto.getEndTime());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreateTime);

        // 查询
        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        // 转换VO
        Page<AdminOrderVO> voPage = new Page<>();
        BeanUtils.copyProperties(orderPage, voPage, "records");

        List<AdminOrderVO> voList = orderPage.getRecords().stream().map(order -> {
            AdminOrderVO vo = convertToVO(order);

            // 查询用户信息
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }

            // 拼接完整地址
            vo.setFullAddress(order.getReceiverProvince() + order.getReceiverCity() +
                    order.getReceiverDistrict() + order.getReceiverAddress());

            // 设置状态名称
            vo.setStatusName(STATUS_MAP.get(order.getStatus()));
            vo.setLogisticsStatusName(LOGISTICS_STATUS_MAP.get(order.getLogisticsStatus()));

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);

        log.info("查询到 {} 条订单", voList.size());
        return voPage;
    }

    @Override
    public AdminOrderVO getOrderDetail(Long orderId) {
        log.info("=== 查询订单详情 ===");
        log.info("订单ID: {}", orderId);

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 转换VO
        AdminOrderVO vo = convertToVO(order);

        // 查询用户信息
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }

        // 拼接完整地址
        vo.setFullAddress(order.getReceiverProvince() + order.getReceiverCity() +
                order.getReceiverDistrict() + order.getReceiverAddress());

        // 设置状态名称
        vo.setStatusName(STATUS_MAP.get(order.getStatus()));
        vo.setLogisticsStatusName(LOGISTICS_STATUS_MAP.get(order.getLogisticsStatus()));

        // 查询订单商品
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        List<OrderItemVO> itemVOList = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList());

        vo.setItems(itemVOList);

        log.info("订单详情查询成功");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(OrderShipDTO dto) {
        log.info("=== 订单发货 ===");
        log.info("发货信息: {}", dto);

        // 查询订单
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 校验订单状态(只有待发货的订单才能发货)
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新订单
        order.setLogisticsCompany(dto.getLogisticsCompany());
        order.setTrackingNumber(dto.getTrackingNumber());
        order.setLogisticsStatus(1); // 运输中
        order.setStatus(2); // 待收货
        order.setDeliveryTime(LocalDateTime.now());

        if (StrUtil.isNotBlank(dto.getRemark())) {
            String adminRemark = order.getAdminRemark();
            if (StrUtil.isBlank(adminRemark)) {
                adminRemark = dto.getRemark();
            } else {
                adminRemark += "\n" + dto.getRemark();
            }
            order.setAdminRemark(adminRemark);
        }

        int count = orderMapper.updateById(order);
        if (count != 1) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        }

        log.info("订单发货成功, 订单号: {}", order.getOrderNo());

        // 🔥 新增：发送发货通知
        notificationHelper.sendOrderShippedNotification(
                order.getUserId(),
                order.getOrderNo(),
                dto.getLogisticsCompany(),
                dto.getTrackingNumber(),
                order.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderCancelDTO dto) {
        log.info("=== 取消订单 ===");
        log.info("取消信息: {}", dto);

        // 查询订单
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 校验订单状态(只有待支付和待发货的订单才能取消)
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态
        order.setStatus(4); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(dto.getCancelReason());

        int count = orderMapper.updateById(order);
        if (count != 1) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        }

        // 如果订单已支付,需要退款
        if (order.getStatus() == 1) {
            // 查询支付记录
            LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Payment::getOrderId, order.getId());
            wrapper.eq(Payment::getStatus, 1); // 支付成功
            Payment payment = paymentMapper.selectOne(wrapper);

            if (payment != null) {
                // 更新支付状态为已退款
                payment.setStatus(3);
                paymentMapper.updateById(payment);
                log.info("订单已支付,自动退款成功");
            }
        }

        // 回滚库存
        rollbackStock(order.getId());

        log.info("订单取消成功, 订单号: {}", order.getOrderNo());

        // 🔥 新增：发送取消通知
        notificationHelper.sendOrderCancelledNotification(
                order.getUserId(),
                order.getOrderNo(),
                order.getId()
        );
    }

    @Override
    public void addRemark(OrderRemarkDTO dto) {
        log.info("=== 添加订单备注 ===");
        log.info("备注信息: {}", dto);

        // 查询订单
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 添加备注
        String adminRemark = order.getAdminRemark();
        if (StrUtil.isBlank(adminRemark)) {
            adminRemark = dto.getRemark();
        } else {
            adminRemark += "\n" + dto.getRemark();
        }
        order.setAdminRemark(adminRemark);

        int count = orderMapper.updateById(order);
        if (count != 1) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        }

        log.info("备注添加成功");
    }

    /**
     * 订单派送
     * 将物流状态从运输中(1)改为派送中(2)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(OrderDeliverDTO dto) {
        log.info("=== 订单派送 ===");
        log.info("订单ID: {}", dto.getOrderId());

        // 1. 查询订单
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 校验订单状态(只有待收货的订单才能派送)
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 3. 校验物流状态(只有运输中的订单才能改为派送中)
        if (order.getLogisticsStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 4. 更新物流状态为派送中
        order.setLogisticsStatus(2);  // 派送中
        int count = orderMapper.updateById(order);
        if (count != 1) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        }

        log.info("订单派送成功, 订单号: {}, 物流状态: 运输中 → 派送中", order.getOrderNo());

        // 🔥 新增：发送派送通知
        notificationHelper.sendOrderDeliveringNotification(
                order.getUserId(),
                order.getTrackingNumber(),
                order.getId()
        );
    }

    @Override
    public void exportOrders(OrderQueryDTO dto, HttpServletResponse response) {
        log.info("=== 导出订单 ===");
        log.info("导出条件: {}", dto);

        try {
            // 查询所有订单(不分页)
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

            // 构建查询条件(同getOrderList)
            if (StrUtil.isNotBlank(dto.getOrderNo())) {
                wrapper.like(Order::getOrderNo, dto.getOrderNo());
            }
            if (dto.getUserId() != null) {
                wrapper.eq(Order::getUserId, dto.getUserId());
            }
            if (StrUtil.isNotBlank(dto.getReceiverName())) {
                wrapper.like(Order::getReceiverName, dto.getReceiverName());
            }
            if (StrUtil.isNotBlank(dto.getReceiverPhone())) {
                wrapper.like(Order::getReceiverPhone, dto.getReceiverPhone());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(Order::getStatus, dto.getStatus());
            }
            if (dto.getLogisticsStatus() != null) {
                wrapper.eq(Order::getLogisticsStatus, dto.getLogisticsStatus());
            }
            if (StrUtil.isNotBlank(dto.getStartTime())) {
                wrapper.ge(Order::getCreateTime, dto.getStartTime());
            }
            if (StrUtil.isNotBlank(dto.getEndTime())) {
                wrapper.le(Order::getCreateTime, dto.getEndTime());
            }

            wrapper.orderByDesc(Order::getCreateTime);

            List<Order> orders = orderMapper.selectList(wrapper);

            // 转换为导出VO
            List<OrderExportVO> exportList = orders.stream().map(order -> {
                OrderExportVO exportVO = new OrderExportVO();
                exportVO.setOrderNo(order.getOrderNo());

                // 查询用户名
                User user = userMapper.selectById(order.getUserId());
                if (user != null) {
                    exportVO.setUsername(user.getUsername());
                }

                exportVO.setReceiverName(order.getReceiverName());
                exportVO.setReceiverPhone(order.getReceiverPhone());
                exportVO.setFullAddress(order.getReceiverProvince() + order.getReceiverCity() +
                        order.getReceiverDistrict() + order.getReceiverAddress());
                exportVO.setTotalAmount(order.getTotalAmount());
                exportVO.setFreight(order.getFreight());
                exportVO.setDiscountAmount(order.getDiscountAmount());
                exportVO.setPaymentAmount(order.getPaymentAmount());
                exportVO.setStatusName(STATUS_MAP.get(order.getStatus()));
                exportVO.setLogisticsCompany(order.getLogisticsCompany());
                exportVO.setTrackingNumber(order.getTrackingNumber());
                exportVO.setLogisticsStatusName(LOGISTICS_STATUS_MAP.get(order.getLogisticsStatus()));
                exportVO.setAdminRemark(order.getAdminRemark());

                // 格式化时间
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                exportVO.setCreateTime(order.getCreateTime() != null ?
                        order.getCreateTime().format(formatter) : "");
                exportVO.setPaymentTime(order.getPaymentTime() != null ?
                        order.getPaymentTime().format(formatter) : "");
                exportVO.setDeliveryTime(order.getDeliveryTime() != null ?
                        order.getDeliveryTime().format(formatter) : "");

                return exportVO;
            }).collect(Collectors.toList());

            // 创建Excel写入器
            ExcelWriter writer = ExcelUtil.getWriter(true);

            // 写入数据
            writer.write(exportList, true);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("订单列表_" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss"), "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 输出
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();

            log.info("导出订单成功, 共 {} 条", exportList.size());

        } catch (IOException e) {
            log.error("导出订单失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 转换为VO
     */
    private AdminOrderVO convertToVO(Order order) {
        AdminOrderVO vo = new AdminOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    /**
     * 回滚库存
     */
    private void rollbackStock(Long orderId) {
        log.info("=== 回滚库存 ===");

        // 查询订单商品
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        // 恢复库存
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
                log.info("商品ID: {}, 恢复库存: {}", product.getId(), item.getQuantity());
            }
        }

        log.info("库存回滚成功");
    }
}
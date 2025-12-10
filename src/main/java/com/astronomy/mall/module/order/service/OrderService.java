package com.astronomy.mall.module.order.service;

import com.astronomy.mall.module.order.dto.CreateOrderDTO;
import com.astronomy.mall.module.order.vo.OrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface OrderService {

    /**
     * 创建订单
     */
    OrderVO createOrder(Long userId, CreateOrderDTO dto);

    /**
     * 查询订单列表(分页)
     */
    Page<OrderVO> getOrderList(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 查询订单详情
     */
    OrderVO getOrderDetail(Long userId, Long orderId);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, Long orderId, String reason);

    /**
     * 确认收货
     */
    void confirmReceipt(Long userId, Long orderId);

    /**
     * 删除订单(仅限已取消或已完成的订单)
     */
    void deleteOrder(Long userId, Long orderId);
}
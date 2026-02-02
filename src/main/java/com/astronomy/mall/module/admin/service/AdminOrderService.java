package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.vo.AdminOrderVO;
import com.astronomy.mall.module.admin.vo.OrderExportVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 管理员订单Service
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
public interface AdminOrderService {

    /**
     * 订单列表(分页)
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<AdminOrderVO> getOrderList(OrderQueryDTO dto);

    /**
     * 订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    AdminOrderVO getOrderDetail(Long orderId);

    /**
     * 订单发货
     *
     * @param dto 发货信息
     */
    void shipOrder(OrderShipDTO dto);

    /**
     * 订单派送
     * 将物流状态从运输中(1)改为派送中(2)
     *
     * @param dto 派送信息
     */
    void deliverOrder(OrderDeliverDTO dto);

    /**
     * 取消订单
     *
     * @param dto 取消信息
     */
    void cancelOrder(OrderCancelDTO dto);

    /**
     * 添加备注
     *
     * @param dto 备注信息
     */
    void addRemark(OrderRemarkDTO dto);

    /**
     * 导出订单
     *
     * @param dto 查询条件
     * @param response HTTP响应
     */
    void exportOrders(OrderQueryDTO dto, HttpServletResponse response);
}
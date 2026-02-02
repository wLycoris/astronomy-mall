package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.service.AdminOrderService;
import com.astronomy.mall.module.admin.vo.AdminOrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 管理员订单Controller
 *
 * @author astronomy-mall
 * @date 2026-01-28
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/order")
@Api(tags = "管理员-订单管理")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping("/list")
    @ApiOperation("订单列表")
    @AdminLog("查询订单列表")
    public Result<Page<AdminOrderVO>> getOrderList(OrderQueryDTO dto) {
        log.info("=== 订单列表 ===");
        Page<AdminOrderVO> page = adminOrderService.getOrderList(dto);
        return Result.success(page);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("订单详情")
    @AdminLog("查询订单详情")
    public Result<AdminOrderVO> getOrderDetail(@PathVariable Long id) {
        log.info("=== 订单详情 ===");
        log.info("订单ID: {}", id);
        AdminOrderVO vo = adminOrderService.getOrderDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/ship")
    @ApiOperation("订单发货")
    @AdminLog(value = "订单发货", recordParams = true)
    public Result<Void> shipOrder(@Valid @RequestBody OrderShipDTO dto) {
        log.info("=== 订单发货 ===");
        log.info("发货信息: {}", dto);
        adminOrderService.shipOrder(dto);
        return Result.success();
    }

    @PostMapping("/cancel")
    @ApiOperation("取消订单")
    @AdminLog(value = "取消订单", recordParams = true)
    public Result<Void> cancelOrder(@Valid @RequestBody OrderCancelDTO dto) {
        log.info("=== 取消订单 ===");
        log.info("取消信息: {}", dto);
        adminOrderService.cancelOrder(dto);
        return Result.success();
    }

    @PostMapping("/remark")
    @ApiOperation("添加备注")
    @AdminLog(value = "添加订单备注", recordParams = true)
    public Result<Void> addRemark(@Valid @RequestBody OrderRemarkDTO dto) {
        log.info("=== 添加订单备注 ===");
        log.info("备注信息: {}", dto);
        adminOrderService.addRemark(dto);
        return Result.success();
    }

    @GetMapping("/export")
    @ApiOperation("导出订单")
    @AdminLog("导出订单")
    public void exportOrders(OrderQueryDTO dto, HttpServletResponse response) {
        log.info("=== 导出订单 ===");
        adminOrderService.exportOrders(dto, response);
    }

    /**
     * 订单派送
     */
    @PostMapping("/deliver")
    @ApiOperation("订单派送")
    @AdminLog("订单派送")
    public Result<Void> deliverOrder(@Valid @RequestBody OrderDeliverDTO dto) {
        log.info("=== 订单派送接口 ===");
        adminOrderService.deliverOrder(dto);
        return Result.success();
    }
}
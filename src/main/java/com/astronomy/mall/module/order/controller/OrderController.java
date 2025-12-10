package com.astronomy.mall.module.order.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.utils.JwtUtil;
import com.astronomy.mall.module.order.dto.CreateOrderDTO;
import com.astronomy.mall.module.order.service.OrderService;
import com.astronomy.mall.module.order.vo.OrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 订单控制器
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    /**
     * 从请求头获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("=== OrderController - getUserIdFromRequest ===");
        System.out.println("Authorization: " + token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            System.out.println("解析到的 userId: " + userId);
            return userId;
        }

        System.out.println("❌ Token不存在或格式错误");
        throw new RuntimeException("未登录");
    }

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<OrderVO> createOrder(
            HttpServletRequest request,
            @ApiParam("创建订单请求") @Valid @RequestBody CreateOrderDTO dto
    ) {
        Long userId = getUserIdFromRequest(request);
        System.out.println("=== OrderController.createOrder ===");
        System.out.println("userId: " + userId);
        System.out.println("cartIds: " + dto.getCartIds());

        OrderVO orderVO = orderService.createOrder(userId, dto);
        return Result.success(orderVO);
    }

    @ApiOperation("查询订单列表")
    @GetMapping("/list")
    public Result<Page<OrderVO>> getOrderList(
            HttpServletRequest request,
            @ApiParam("订单状态(0-待支付, 1-待发货, 2-待收货, 3-已完成, 4-已取消)") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Long userId = getUserIdFromRequest(request);
        Page<OrderVO> page = orderService.getOrderList(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/detail/{orderId}")
    public Result<OrderVO> getOrderDetail(
            HttpServletRequest request,
            @ApiParam("订单ID") @PathVariable Long orderId
    ) {
        Long userId = getUserIdFromRequest(request);
        OrderVO orderVO = orderService.getOrderDetail(userId, orderId);
        return Result.success(orderVO);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{orderId}")
    public Result<Void> cancelOrder(
            HttpServletRequest request,
            @ApiParam("订单ID") @PathVariable Long orderId,
            @ApiParam("取消原因") @RequestParam String reason
    ) {
        Long userId = getUserIdFromRequest(request);
        orderService.cancelOrder(userId, orderId, reason);
        return Result.success();
    }

    @ApiOperation("确认收货")
    @PutMapping("/confirmReceipt/{orderId}")
    public Result<Void> confirmReceipt(
            HttpServletRequest request,
            @ApiParam("订单ID") @PathVariable Long orderId
    ) {
        Long userId = getUserIdFromRequest(request);
        orderService.confirmReceipt(userId, orderId);
        return Result.success();
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/delete/{orderId}")
    public Result<Void> deleteOrder(
            HttpServletRequest request,
            @ApiParam("订单ID") @PathVariable Long orderId
    ) {
        Long userId = getUserIdFromRequest(request);
        orderService.deleteOrder(userId, orderId);
        return Result.success();
    }
}
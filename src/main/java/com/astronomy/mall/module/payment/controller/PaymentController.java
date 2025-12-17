package com.astronomy.mall.module.payment.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.payment.dto.*;
import com.astronomy.mall.module.payment.service.PaymentService;
import com.astronomy.mall.module.payment.service.RefundService;
import com.astronomy.mall.module.payment.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 支付控制器
 */
@Slf4j
@Api(tags = "支付管理")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefundService refundService;

    /**
     * 创建支付订单
     */
    @ApiOperation("创建支付订单")
    @PostMapping("/create")
    public Result<PaymentVO> createPayment(@Validated @RequestBody CreatePaymentDTO dto,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PaymentVO paymentVO = paymentService.createPayment(dto, userId);
        return Result.success(paymentVO);
    }

    /**
     * 模拟支付成功(仅用于测试)
     */
    @ApiOperation("模拟支付成功")
    @PostMapping("/simulate/{paymentId}")
    public Result<Void> simulatePayment(@PathVariable Long paymentId,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        paymentService.simulatePaymentSuccess(paymentId, userId);
        return Result.success();
    }

    /**
     * 查询支付状态
     */
    @ApiOperation("查询支付状态")
    @GetMapping("/status/{paymentId}")
    public Result<PaymentVO> getPaymentStatus(@PathVariable Long paymentId,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PaymentVO paymentVO = paymentService.getPaymentStatus(paymentId, userId);
        return Result.success(paymentVO);
    }

    /**
     * 根据订单ID查询支付记录
     */
    @ApiOperation("根据订单ID查询支付记录")
    @GetMapping("/order/{orderId}")
    public Result<PaymentVO> getPaymentByOrderId(@PathVariable Long orderId,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PaymentVO paymentVO = paymentService.getPaymentByOrderId(orderId, userId);
        return Result.success(paymentVO);
    }

    /**
     * 分页查询支付记录
     */
    @ApiOperation("分页查询支付记录")
    @GetMapping("/list")
    public Result<Page<PaymentVO>> getPaymentList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<PaymentVO> page = paymentService.getPaymentList(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 申请退款
     */
    @ApiOperation("申请退款")
    @PostMapping("/refund/apply")
    public Result<RefundVO> applyRefund(@Validated @RequestBody ApplyRefundDTO dto,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RefundVO refundVO = refundService.applyRefund(dto, userId);
        return Result.success(refundVO);
    }

    /**
     * 查询退款详情
     */
    @ApiOperation("查询退款详情")
    @GetMapping("/refund/detail/{refundId}")
    public Result<RefundVO> getRefundDetail(@PathVariable Long refundId,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RefundVO refundVO = refundService.getRefundDetail(refundId, userId);
        return Result.success(refundVO);
    }

    /**
     * 根据订单ID查询退款记录
     */
    @ApiOperation("根据订单ID查询退款记录")
    @GetMapping("/refund/order/{orderId}")
    public Result<RefundVO> getRefundByOrderId(@PathVariable Long orderId,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        RefundVO refundVO = refundService.getRefundByOrderId(orderId, userId);
        return Result.success(refundVO);
    }

    /**
     * 分页查询退款列表
     */
    @ApiOperation("分页查询退款列表")
    @GetMapping("/refund/list")
    public Result<Page<RefundVO>> getRefundList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<RefundVO> page = refundService.getRefundList(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 取消退款申请
     */
    @ApiOperation("取消退款申请")
    @PostMapping("/refund/cancel/{refundId}")
    public Result<Void> cancelRefund(@PathVariable Long refundId,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        refundService.cancelRefund(refundId, userId);
        return Result.success();
    }
}
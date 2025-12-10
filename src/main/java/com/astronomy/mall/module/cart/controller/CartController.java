package com.astronomy.mall.module.cart.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.utils.JwtUtil;
import com.astronomy.mall.module.cart.service.CartService;
import com.astronomy.mall.module.cart.vo.CartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 购物车控制器
 */
@Api(tags = "购物车管理")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    /**
     * 从请求头获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未登录");
    }

    @ApiOperation("添加商品到购物车")
    @PostMapping("/add")
    public Result<Void> addToCart(
            HttpServletRequest request,
            @ApiParam("商品ID") @RequestParam @NotNull Long productId,
            @ApiParam("数量") @RequestParam @Min(1) Integer quantity
    ) {
        Long userId = getUserIdFromRequest(request);
        System.out.println("=== CartController.addToCart ===");
        System.out.println("userId: " + userId);
        System.out.println("productId: " + productId);
        System.out.println("quantity: " + quantity);

        cartService.addToCart(userId, productId, quantity);
        return Result.success();
    }

    @ApiOperation("查询购物车列表")
    @GetMapping("/list")
    public Result<List<CartVO>> getCartList(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<CartVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    @ApiOperation("更新购物车商品数量")
    @PutMapping("/updateQuantity")
    public Result<Void> updateQuantity(
            HttpServletRequest request,
            @ApiParam("购物车ID") @RequestParam @NotNull Long cartId,
            @ApiParam("数量") @RequestParam @Min(1) Integer quantity
    ) {
        Long userId = getUserIdFromRequest(request);
        cartService.updateQuantity(userId, cartId, quantity);
        return Result.success();
    }

    @ApiOperation("切换购物车项选中状态")
    @PutMapping("/toggleSelected")
    public Result<Void> toggleSelected(
            HttpServletRequest request,
            @ApiParam("购物车ID") @RequestParam @NotNull Long cartId,
            @ApiParam("是否选中(0-未选中, 1-已选中)") @RequestParam @NotNull Integer selected
    ) {
        Long userId = getUserIdFromRequest(request);
        cartService.toggleSelected(userId, cartId, selected);
        return Result.success();
    }

    @ApiOperation("全选/取消全选")
    @PutMapping("/selectAll")
    public Result<Void> selectAll(
            HttpServletRequest request,
            @ApiParam("是否选中(0-未选中, 1-已选中)") @RequestParam @NotNull Integer selected
    ) {
        Long userId = getUserIdFromRequest(request);
        cartService.selectAll(userId, selected);
        return Result.success();
    }

    @ApiOperation("删除购物车项")
    @DeleteMapping("/remove/{cartId}")
    public Result<Void> removeCartItem(
            HttpServletRequest request,
            @ApiParam("购物车ID") @PathVariable Long cartId
    ) {
        Long userId = getUserIdFromRequest(request);
        cartService.removeCartItem(userId, cartId);
        return Result.success();
    }

    @ApiOperation("批量删除购物车项")
    @DeleteMapping("/batchRemove")
    public Result<Void> batchRemove(
            HttpServletRequest request,
            @ApiParam("购物车ID列表") @RequestBody List<Long> cartIds
    ) {
        Long userId = getUserIdFromRequest(request);
        cartService.batchRemove(userId, cartIds);
        return Result.success();
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        cartService.clearCart(userId);
        return Result.success();
    }
}
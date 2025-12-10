package com.astronomy.mall.module.cart.service;

import com.astronomy.mall.module.cart.vo.CartVO;
import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 查询购物车列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 更新购物车商品数量
     */
    void updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 切换购物车项选中状态
     */
    void toggleSelected(Long userId, Long cartId, Integer selected);

    /**
     * 全选/取消全选
     */
    void selectAll(Long userId, Integer selected);

    /**
     * 删除购物车项
     */
    void removeCartItem(Long userId, Long cartId);

    /**
     * 批量删除购物车项
     */
    void batchRemove(Long userId, List<Long> cartIds);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);
}
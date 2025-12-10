package com.astronomy.mall.module.cart.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.cart.entity.Cart;
import com.astronomy.mall.module.cart.mapper.CartMapper;
import com.astronomy.mall.module.cart.service.CartService;
import com.astronomy.mall.module.cart.vo.CartVO;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long userId, Long productId, Integer quantity) {
        // 1. 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(2001, "商品不存在");
        }

        // 2. 检查库存
        if (product.getStock() < quantity) {
            throw new BusinessException(2002, "库存不足");
        }

        // 3. 检查是否已在购物车
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId);
        Cart existCart = cartMapper.selectOne(wrapper);

        if (existCart != null) {
            // 已存在,数量相加
            int newQuantity = existCart.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new BusinessException(2002, "库存不足");
            }
            existCart.setQuantity(newQuantity);
            cartMapper.updateById(existCart);
        } else {
            // 新增购物车项
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cartMapper.insert(cart);
        }
    }

    @Override
    public List<CartVO> getCartList(Long userId) {
        List<CartVO> cartList = cartMapper.selectCartListWithProduct(userId);

        // 计算小计
        cartList.forEach(cart -> {
            BigDecimal subtotal = cart.getProductPrice()
                    .multiply(BigDecimal.valueOf(cart.getQuantity()));
            cart.setSubtotal(subtotal);
        });

        return cartList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        // 1. 查询购物车项
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(2201, "购物车项不存在");
        }

        // 2. 检查库存
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null) {
            throw new BusinessException(2001, "商品不存在");
        }
        if (quantity > product.getStock()) {
            throw new BusinessException(2002, "库存不足");
        }

        // 3. 更新数量
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleSelected(Long userId, Long cartId, Integer selected) {
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getId, cartId)
                .set(Cart::getSelected, selected);
        cartMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectAll(Long userId, Integer selected) {
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .set(Cart::getSelected, selected);
        cartMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItem(Long userId, Long cartId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getId, cartId);
        cartMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemove(Long userId, List<Long> cartIds) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).in(Cart::getId, cartIds);
        cartMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }
}
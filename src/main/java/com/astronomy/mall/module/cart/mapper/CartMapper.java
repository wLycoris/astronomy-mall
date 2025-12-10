package com.astronomy.mall.module.cart.mapper;

import com.astronomy.mall.module.cart.entity.Cart;
import com.astronomy.mall.module.cart.vo.CartVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 查询用户购物车列表(关联商品信息)
     */
    List<CartVO> selectCartListWithProduct(@Param("userId") Long userId);

    /**
     * 查询用户选中的购物车项
     */
    List<CartVO> selectSelectedCartItems(@Param("userId") Long userId);
}
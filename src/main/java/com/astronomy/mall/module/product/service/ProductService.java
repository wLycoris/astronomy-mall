package com.astronomy.mall.module.product.service;

import com.astronomy.mall.module.product.dto.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface ProductService {

    /**
     * 分页查询商品列表
     */
    Page<ProductVO> getProductList(ProductQueryDTO queryDTO);

    /**
     * 获取商品详情
     */
    ProductDetailVO getProductDetail(Long productId);

    /**
     * 获取推荐商品列表
     */
    Page<ProductVO> getRecommendProducts(Integer pageNum, Integer pageSize);

    /**
     * 获取热卖商品列表
     */
    Page<ProductVO> getHotProducts(Integer pageNum, Integer pageSize);

    /**
     * 获取新品列表
     */
    Page<ProductVO> getNewProducts(Integer pageNum, Integer pageSize);
}
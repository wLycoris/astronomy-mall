package com.astronomy.mall.module.product.service;

import com.astronomy.mall.module.product.dto.ReviewStatisticsVO;
import com.astronomy.mall.module.product.dto.ReviewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface ReviewService {

    /**
     * 分页获取商品评价列表
     */
    Page<ReviewVO> getProductReviews(Long productId, Integer pageNum, Integer pageSize);

    /**
     * 获取商品评价统计
     */
    ReviewStatisticsVO getReviewStatistics(Long productId);
}
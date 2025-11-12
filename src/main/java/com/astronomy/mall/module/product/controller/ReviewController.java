package com.astronomy.mall.module.product.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.product.dto.ReviewStatisticsVO;
import com.astronomy.mall.module.product.dto.ReviewVO;
import com.astronomy.mall.module.product.service.ReviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/review")
@Api(tags = "商品评价接口")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    @GetMapping("/list/{productId}")
    @ApiOperation("分页获取商品评价列表")
    public Result<Page<ReviewVO>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ReviewVO> page = reviewService.getProductReviews(productId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/statistics/{productId}")
    @ApiOperation("获取商品评价统计")
    public Result<ReviewStatisticsVO> getReviewStatistics(@PathVariable Long productId) {
        ReviewStatisticsVO statistics = reviewService.getReviewStatistics(productId);
        return Result.success(statistics);
    }
}
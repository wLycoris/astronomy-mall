package com.astronomy.mall.module.product.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.product.dto.*;
import com.astronomy.mall.module.product.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/product")
@Api(tags = "商品管理接口")
public class ProductController {

    @Resource
    private ProductService productService;

    @PostMapping("/list")
    @ApiOperation("分页查询商品列表")
    public Result<Page<ProductVO>> getProductList(@RequestBody ProductQueryDTO queryDTO) {
        Page<ProductVO> page = productService.getProductList(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取商品详情")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long id) {
        ProductDetailVO detail = productService.getProductDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/recommend")
    @ApiOperation("获取推荐商品列表")
    public Result<Page<ProductVO>> getRecommendProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ProductVO> page = productService.getRecommendProducts(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/hot")
    @ApiOperation("获取热卖商品列表")
    public Result<Page<ProductVO>> getHotProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ProductVO> page = productService.getHotProducts(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/new")
    @ApiOperation("获取新品列表")
    public Result<Page<ProductVO>> getNewProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ProductVO> page = productService.getNewProducts(pageNum, pageSize);
        return Result.success(page);
    }
}
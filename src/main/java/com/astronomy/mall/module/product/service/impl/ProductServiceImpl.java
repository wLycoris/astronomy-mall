package com.astronomy.mall.module.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.product.dto.*;
import com.astronomy.mall.module.product.entity.Category;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.CategoryMapper;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.astronomy.mall.module.product.mapper.ReviewMapper;
import com.astronomy.mall.module.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ReviewMapper reviewMapper;

    @Override
    public Page<ProductVO> getProductList(ProductQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只查询上架商品

        // ✅ 修复: 一级分类查询时,包含所有子分类的商品
        if (queryDTO.getCategoryId() != null) {
            // 查询该分类信息
            Category category = categoryMapper.selectById(queryDTO.getCategoryId());
            if (category != null) {
                if (category.getLevel() == 1) {
                    // 一级分类: 查询该分类及其所有子分类的商品
                    List<Long> categoryIds = new ArrayList<>();
                    categoryIds.add(category.getId());

                    // 查找所有子分类
                    LambdaQueryWrapper<Category> catWrapper = new LambdaQueryWrapper<>();
                    catWrapper.eq(Category::getParentId, category.getId());
                    List<Category> subCategories = categoryMapper.selectList(catWrapper);
                    for (Category subCat : subCategories) {
                        categoryIds.add(subCat.getId());
                    }

                    // 使用 IN 查询
                    wrapper.in(Product::getCategoryId, categoryIds);
                } else {
                    // 二级分类: 只查询该分类的商品
                    wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
                }
            }
        }
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Product::getProductName, queryDTO.getKeyword())
                    .or().like(Product::getBrand, queryDTO.getKeyword()));
        }
        if (StrUtil.isNotBlank(queryDTO.getBrand())) {
            wrapper.eq(Product::getBrand, queryDTO.getBrand());
        }
        if (queryDTO.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, queryDTO.getMinPrice());
        }
        if (queryDTO.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, queryDTO.getMaxPrice());
        }
        if (queryDTO.getIsHot() != null) {
            wrapper.eq(Product::getIsHot, queryDTO.getIsHot());
        }
        if (queryDTO.getIsNew() != null) {
            wrapper.eq(Product::getIsNew, queryDTO.getIsNew());
        }
        if (queryDTO.getIsRecommend() != null) {
            wrapper.eq(Product::getIsRecommend, queryDTO.getIsRecommend());
        }

        // 2. 排序
        if (StrUtil.isNotBlank(queryDTO.getSortField())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getSortOrder());
            switch (queryDTO.getSortField()) {
                case "price":
                    wrapper.orderBy(true, isAsc, Product::getPrice);
                    break;
                case "sales":
                    wrapper.orderBy(true, isAsc, Product::getSales);
                    break;
                case "create_time":
                    wrapper.orderBy(true, isAsc, Product::getCreateTime);
                    break;
            }
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

        // 3. 分页查询
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        // 4. 转换为VO
        Page<ProductVO> result = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<ProductVO> voList = productPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        result.setRecords(voList);

        return result;
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        // 1. 查询商品信息
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 增加浏览次数
        productMapper.increaseViewCount(productId);

        // 3. 查询分类名称
        Category category = categoryMapper.selectById(product.getCategoryId());

        // 4. 查询评价统计
        Map<String, Object> reviewStats = reviewMapper.getReviewStatistics(productId);

        // 5. 转换为DetailVO
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);
        vo.setCategoryName(category != null ? category.getCategoryName() : "");

        // 处理图片列表
        vo.setImageList(parseProductImages(product));

        // 安全转换评价统计
        Number reviewCount = (Number) reviewStats.get("reviewCount");
        Number avgRating = (Number) reviewStats.get("avgRating");
        Number goodRate = (Number) reviewStats.get("goodRate");

        vo.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);
        vo.setAvgRating(avgRating != null ? avgRating.doubleValue() : 0.0);
        vo.setGoodRate(goodRate != null ? goodRate.doubleValue() : 0.0);

        return vo;
    }


    @Override
    public Page<ProductVO> getRecommendProducts(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .eq(Product::getIsRecommend, 1)
                .orderByDesc(Product::getSales);

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        return convertToVOPage(productPage);
    }

    @Override
    public Page<ProductVO> getHotProducts(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .eq(Product::getIsHot, 1)
                .orderByDesc(Product::getSales);

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        return convertToVOPage(productPage);
    }

    @Override
    public Page<ProductVO> getNewProducts(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .eq(Product::getIsNew, 1)
                .orderByDesc(Product::getCreateTime);

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        return convertToVOPage(productPage);
    }

    /**
     * Entity转VO
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);

        // 查询评价统计
        Map<String, Object> reviewStats = reviewMapper.getReviewStatistics(product.getId());

        // reviewCount 本身是 COUNT(*)，MyBatis 映射为 Long/BigInteger
        Object reviewCountObj = reviewStats.get("reviewCount");
        vo.setReviewCount(reviewCountObj == null ? 0 : ((Number) reviewCountObj).intValue());

        // avgRating 转换 BigDecimal -> Double
        Object avgRatingObj = reviewStats.get("avgRating");
        vo.setAvgRating(avgRatingObj == null ? 0.0 : ((BigDecimal) avgRatingObj).doubleValue());

        return vo;
    }

    private List<String> parseProductImages(Product product) {
        List<String> images = new ArrayList<>();
        if (StrUtil.isNotBlank(product.getImages())) {
            String raw = product.getImages().trim();
            try {
                if (raw.startsWith("[")) {
                    images.addAll(JSON.parseArray(raw, String.class));
                } else {
                    images.addAll(Arrays.asList(raw.split(",")));
                }
            } catch (Exception ignored) {
                images.addAll(Arrays.asList(raw.split(",")));
            }
        }
        images = images.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .filter(item -> item.startsWith("data:image")
                        || item.startsWith("http://")
                        || item.startsWith("https://")
                        || item.startsWith("/"))
                .distinct()
                .collect(Collectors.toList());
        if (StrUtil.isNotBlank(product.getMainImage()) && !images.contains(product.getMainImage())) {
            images.add(0, product.getMainImage());
        }
        return images.isEmpty() ? Collections.emptyList() : images;
    }

    /**
     * Page<Product> 转 Page<ProductVO>
     */
    private Page<ProductVO> convertToVOPage(Page<Product> productPage) {
        Page<ProductVO> result = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<ProductVO> voList = productPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        result.setRecords(voList);
        return result;
    }
}

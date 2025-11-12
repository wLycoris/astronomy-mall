package com.astronomy.mall.module.product.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.product.dto.CategoryVO;
import com.astronomy.mall.module.product.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/category")
@Api(tags = "商品分类接口")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping("/tree")
    @ApiOperation("获取分类树(包含子分类)")
    public Result<List<CategoryVO>> getCategoryTree() {
        List<CategoryVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @GetMapping("/first-level")
    @ApiOperation("获取一级分类列表")
    public Result<List<CategoryVO>> getFirstLevelCategories() {
        List<CategoryVO> categories = categoryService.getFirstLevelCategories();
        return Result.success(categories);
    }

    @GetMapping("/children/{parentId}")
    @ApiOperation("根据父分类ID获取子分类")
    public Result<List<CategoryVO>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryVO> categories = categoryService.getCategoriesByParentId(parentId);
        return Result.success(categories);
    }
}
package com.astronomy.mall.module.product.service;

import com.astronomy.mall.module.product.dto.CategoryVO;
import java.util.List;

public interface CategoryService {

    /**
     * 获取所有分类树(包含子分类)
     */
    List<CategoryVO> getCategoryTree();

    /**
     * 获取一级分类列表
     */
    List<CategoryVO> getFirstLevelCategories();

    /**
     * 根据父分类ID获取子分类
     */
    List<CategoryVO> getCategoriesByParentId(Long parentId);
}
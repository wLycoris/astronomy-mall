package com.astronomy.mall.module.product.service.impl;

import com.astronomy.mall.module.product.dto.CategoryVO;
import com.astronomy.mall.module.product.entity.Category;
import com.astronomy.mall.module.product.mapper.CategoryMapper;
import com.astronomy.mall.module.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getCategoryTree() {
        // 1. 获取所有显示的分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsShow, 1)
                .orderByDesc(Category::getSort);
        List<Category> allCategories = categoryMapper.selectList(wrapper);

        // 2. 转换为VO并构建树形结构
        return buildCategoryTree(allCategories, 0L);
    }

    @Override
    public List<CategoryVO> getFirstLevelCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, 0)
                .eq(Category::getIsShow, 1)
                .orderByDesc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getCategoriesByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId)
                .eq(Category::getIsShow, 1)
                .orderByDesc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 构建分类树
     */
    private List<CategoryVO> buildCategoryTree(List<Category> allCategories, Long parentId) {
        List<CategoryVO> result = new ArrayList<>();

        for (Category category : allCategories) {
            if (category.getParentId().equals(parentId)) {
                CategoryVO vo = convertToVO(category);
                // 递归查找子分类
                List<CategoryVO> children = buildCategoryTree(allCategories, category.getId());
                if (!children.isEmpty()) {
                    vo.setChildren(children);
                }
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * Entity转VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.AddCategoryDTO;
import com.astronomy.mall.module.admin.dto.SortCategoryDTO;
import com.astronomy.mall.module.admin.vo.CategoryTreeVO;

import java.util.List;

/**
 * 后台分类管理 Service 接口
 *
 * 📌 提供分类树查询、新增、编辑、删除、排序功能
 * 📌 删除前检查关联商品，支持级联删除二级分类
 */
public interface AdminCategoryService {

    /**
     * 获取分类树（一级分类 + 子分类，含商品数量）
     *
     * @return 分类树列表（仅一级分类，children 中存放二级）
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 新增分类
     *
     * 📌 parentId=0 时新增一级分类，level=1
     * 📌 parentId!=0 时新增二级分类，level=2
     *
     * @param dto 分类信息
     */
    void addCategory(AddCategoryDTO dto);

    /**
     * 编辑分类
     *
     * 📌 不允许修改 parentId（层级不可变）
     * 📌 可修改：名称、图标、排序、描述、是否显示
     *
     * @param id  分类ID
     * @param dto 分类信息
     */
    void updateCategory(Long id, AddCategoryDTO dto);

    /**
     * 删除分类
     *
     * 📌 删除前检查：该分类及子分类下是否有关联商品
     * 📌 如果是一级分类，级联逻辑删除所有二级子分类
     * 📌 有关联商品则抛出 BusinessException 提示
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 批量排序
     *
     * 📌 前端拖拽排序后批量更新 sort 字段
     *
     * @param dto 排序列表
     */
    void sortCategories(SortCategoryDTO dto);
}
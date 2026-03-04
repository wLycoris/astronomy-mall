package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.AddCategoryDTO;
import com.astronomy.mall.module.admin.dto.SortCategoryDTO;
import com.astronomy.mall.module.admin.service.AdminCategoryService;
import com.astronomy.mall.module.admin.vo.CategoryTreeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台分类管理 ServiceImpl
 *
 * 📌 分类最多两级：一级 (parentId=0, level=1) 和 二级 (parentId=一级ID, level=2)
 * 📌 删除一级分类时级联逻辑删除其所有二级子分类
 * 📌 有关联商品时拒绝删除（给出商品数量提示）
 *
 * 🐛 Bug修复记录 (2026-03-03):
 *    原来在第二步组装树时，对一级分类执行 setChildren(new ArrayList<>())，
 *    但此时子分类可能已经 add 进了旧的 children list（因为sort降序导致子分类先被遍历），
 *    setChildren 会把已添加的子分类全部覆盖清空。
 *    修复方案：在第一步存入 voMap 时就统一初始化 children，第二步不再 setChildren。
 */
@Slf4j
@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    @Autowired
    private com.astronomy.mall.module.product.mapper.CategoryMapper categoryMapper;

    @Autowired
    private com.astronomy.mall.module.product.mapper.ProductMapper productMapper;

    // =========================================================================
    // 1. 获取分类树
    // =========================================================================

    @Override
    public List<CategoryTreeVO> getCategoryTree() {

        // 1) 查询所有未删除的分类
        //    📌 @TableLogic 已自动加 deleted=0，无需手动添加
        //    📌 按 sort 降序、id 升序排列（保证同sort值下顺序稳定）
        LambdaQueryWrapper<com.astronomy.mall.module.product.entity.Category> wrapper =
                new LambdaQueryWrapper<com.astronomy.mall.module.product.entity.Category>()
                        .orderByDesc(com.astronomy.mall.module.product.entity.Category::getSort)
                        .orderByAsc(com.astronomy.mall.module.product.entity.Category::getId);

        List<com.astronomy.mall.module.product.entity.Category> allCategories =
                categoryMapper.selectList(wrapper);

        // 2) 统计每个分类下的商品数量
        Map<Long, Integer> productCountMap = getProductCountMap(allCategories);

        // 3) 所有节点转为 VO，存入 voMap
        //    ✅ 关键修复：在这里统一初始化 children，后面不再 setChildren
        //    原来的 bug：第二步对一级分类执行 setChildren(new ArrayList<>())，
        //    会把已经 add 进去的子分类全部清空
        Map<Long, CategoryTreeVO> voMap = new LinkedHashMap<>();
        for (com.astronomy.mall.module.product.entity.Category category : allCategories) {
            CategoryTreeVO vo = convertToVO(category, productCountMap);
            vo.setChildren(new ArrayList<>());  // ✅ 统一在这里初始化，后面不再覆盖
            voMap.put(category.getId(), vo);
        }

        // 4) 组装树形结构
        List<CategoryTreeVO> tree = new ArrayList<>();
        for (CategoryTreeVO vo : voMap.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                // 一级分类：直接加入结果列表
                // ✅ 不再执行 setChildren(new ArrayList<>())，避免覆盖已添加的子分类
                tree.add(vo);
            } else {
                // 二级分类：找到父节点，加入父节点的 children
                CategoryTreeVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
                // parent == null：父分类被删除或隐藏，跳过，不报错
            }
        }

        return tree;
    }

    // =========================================================================
    // 2. 新增分类
    // =========================================================================

    @Override
    public void addCategory(AddCategoryDTO dto) {

        // 校验：如果是二级分类，父分类必须存在且为一级
        if (dto.getParentId() != null && dto.getParentId() != 0) {
            com.astronomy.mall.module.product.entity.Category parent =
                    categoryMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            if (parent.getLevel() != 1) {
                throw new BusinessException("只支持两级分类，不能在二级分类下再新增子分类");
            }
        }

        // 校验：同一父分类下名称不能重复
        long nameCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<com.astronomy.mall.module.product.entity.Category>()
                        .eq(com.astronomy.mall.module.product.entity.Category::getCategoryName,
                                dto.getCategoryName())
                        .eq(com.astronomy.mall.module.product.entity.Category::getParentId,
                                dto.getParentId() == null ? 0 : dto.getParentId())
        );
        if (nameCount > 0) {
            throw new BusinessException("同级分类下已存在名称为【" + dto.getCategoryName() + "】的分类");
        }

        // 构建实体并插入
        com.astronomy.mall.module.product.entity.Category category =
                new com.astronomy.mall.module.product.entity.Category();
        category.setCategoryName(dto.getCategoryName());
        category.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        category.setLevel(category.getParentId() == 0 ? 1 : 2);
        category.setIcon(dto.getIcon());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        category.setDescription(dto.getDescription());
        category.setIsShow(dto.getIsShow() == null ? 1 : dto.getIsShow());

        categoryMapper.insert(category);
        log.info("新增分类成功：{}", category.getCategoryName());
    }

    // =========================================================================
    // 3. 编辑分类
    // =========================================================================

    @Override
    public void updateCategory(Long id, AddCategoryDTO dto) {

        // 查询目标分类（@TableLogic 自动过滤 deleted=1）
        com.astronomy.mall.module.product.entity.Category existing =
                categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        // 校验：同级分类下名称不能重复（排除自身）
        long nameCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<com.astronomy.mall.module.product.entity.Category>()
                        .eq(com.astronomy.mall.module.product.entity.Category::getCategoryName,
                                dto.getCategoryName())
                        .eq(com.astronomy.mall.module.product.entity.Category::getParentId,
                                existing.getParentId())
                        .ne(com.astronomy.mall.module.product.entity.Category::getId, id)
        );
        if (nameCount > 0) {
            throw new BusinessException("同级分类下已存在名称为【" + dto.getCategoryName() + "】的分类");
        }

        // 更新字段（不允许修改 parentId 和 level）
        existing.setCategoryName(dto.getCategoryName());
        if (dto.getIcon() != null) {
            existing.setIcon(dto.getIcon());
        }
        if (dto.getSort() != null) {
            existing.setSort(dto.getSort());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getIsShow() != null) {
            existing.setIsShow(dto.getIsShow());
        }

        categoryMapper.updateById(existing);
        log.info("编辑分类成功：id={}, name={}", id, dto.getCategoryName());
    }

    // =========================================================================
    // 4. 删除分类（级联删除子分类，删除前检查商品）
    // =========================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {

        // 查询目标分类
        com.astronomy.mall.module.product.entity.Category category =
                categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 收集需要检查和删除的分类ID列表（含子分类）
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(id);

        if (category.getLevel() == 1) {
            // 一级分类：收集所有二级子分类ID
            List<com.astronomy.mall.module.product.entity.Category> children =
                    categoryMapper.selectList(
                            new LambdaQueryWrapper<com.astronomy.mall.module.product.entity.Category>()
                                    .eq(com.astronomy.mall.module.product.entity.Category::getParentId, id)
                    );
            children.forEach(c -> categoryIds.add(c.getId()));
        }

        // 检查这些分类下是否有关联商品
        long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<com.astronomy.mall.module.product.entity.Product>()
                        .in(com.astronomy.mall.module.product.entity.Product::getCategoryId, categoryIds)
        );
        if (productCount > 0) {
            throw new BusinessException(
                    "该分类下还有 " + productCount + " 个商品，请先处理商品后再删除分类");
        }

        // 逻辑删除（MyBatis-Plus deleteById 会自动走 @TableLogic）
        // 但这里需要批量删除，用 update 方式手动设置 deleted=1
        categoryMapper.update(null,
                new LambdaUpdateWrapper<com.astronomy.mall.module.product.entity.Category>()
                        .in(com.astronomy.mall.module.product.entity.Category::getId, categoryIds)
                        .set(com.astronomy.mall.module.product.entity.Category::getDeleted, 1)
        );

        log.info("删除分类成功：ids={}", categoryIds);
    }

    // =========================================================================
    // 5. 批量排序
    // =========================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortCategories(SortCategoryDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return;
        }
        for (SortCategoryDTO.SortItem item : dto.getItems()) {
            categoryMapper.update(null,
                    new LambdaUpdateWrapper<com.astronomy.mall.module.product.entity.Category>()
                            .eq(com.astronomy.mall.module.product.entity.Category::getId, item.getId())
                            .set(com.astronomy.mall.module.product.entity.Category::getSort, item.getSort())
            );
        }
        log.info("分类排序更新成功，共 {} 条", dto.getItems().size());
    }

    // =========================================================================
    // 私有辅助方法
    // =========================================================================

    /**
     * 统计每个分类下的商品数量
     * 📌 @TableLogic 自动过滤 deleted=1 的商品
     */
    private Map<Long, Integer> getProductCountMap(
            List<com.astronomy.mall.module.product.entity.Category> allCategories) {

        if (allCategories.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> allIds = allCategories.stream()
                .map(com.astronomy.mall.module.product.entity.Category::getId)
                .collect(Collectors.toList());

        List<Map<String, Object>> countList = productMapper.selectMaps(
                new QueryWrapper<com.astronomy.mall.module.product.entity.Product>()
                        .select("category_id", "COUNT(*) AS cnt")
                        .in("category_id", allIds)
                        .groupBy("category_id")
        );

        Map<Long, Integer> countMap = new HashMap<>();
        for (Map<String, Object> row : countList) {
            Long categoryId = Long.parseLong(row.get("category_id").toString());
            Integer cnt = Integer.parseInt(row.get("cnt").toString());
            countMap.put(categoryId, cnt);
        }

        return countMap;
    }

    /**
     * Category 实体 → CategoryTreeVO
     */
    private CategoryTreeVO convertToVO(
            com.astronomy.mall.module.product.entity.Category category,
            Map<Long, Integer> productCountMap) {

        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(category.getId());
        vo.setCategoryName(category.getCategoryName());
        vo.setParentId(category.getParentId());
        vo.setLevel(category.getLevel());
        vo.setIcon(category.getIcon());
        vo.setSort(category.getSort());
        vo.setDescription(category.getDescription());
        vo.setIsShow(category.getIsShow());
        vo.setCreateTime(category.getCreateTime());
        vo.setUpdateTime(category.getUpdateTime());
        vo.setProductCount(productCountMap.getOrDefault(category.getId(), 0));
        return vo;
    }
}
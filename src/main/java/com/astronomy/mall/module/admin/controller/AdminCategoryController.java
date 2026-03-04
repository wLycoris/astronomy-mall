package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.AddCategoryDTO;
import com.astronomy.mall.module.admin.dto.SortCategoryDTO;
import com.astronomy.mall.module.admin.service.AdminCategoryService;
import com.astronomy.mall.module.admin.vo.CategoryTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台分类管理 Controller
 *
 * 📌 接口列表（5个）：
 *   GET    /api/admin/category/tree         - 分类树（一级+二级，含商品数量）
 *   POST   /api/admin/category/add          - 新增分类
 *   PUT    /api/admin/category/update/:id   - 编辑分类
 *   DELETE /api/admin/category/delete/:id   - 删除分类（级联删除子分类，检查商品）
 *   POST   /api/admin/category/sort         - 批量排序
 *
 * 📌 所有接口需要管理员权限（由 AdminInterceptor 拦截校验）
 * 📌 操作记录通过 @AdminLog 注解自动写入 tb_admin_log
 */
@RestController
@RequestMapping("/api/admin/category")
@Api(tags = "后台分类管理")
public class AdminCategoryController {

    @Autowired
    private AdminCategoryService adminCategoryService;

    // =========================================================================
    // 1. 分类树
    // =========================================================================

    /**
     * 获取分类树
     *
     * 📌 返回所有未删除的分类，树形结构（一级分类包含 children 子分类）
     * 📌 每个分类节点包含关联商品数量，用于删除前提示
     *
     * @return 分类树列表
     */
    @GetMapping("/tree")
    @ApiOperation("获取分类树")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        return Result.success(adminCategoryService.getCategoryTree());
    }

    // =========================================================================
    // 2. 新增分类
    // =========================================================================

    /**
     * 新增分类
     *
     * 📌 parentId=0 → 一级分类；parentId!=0 → 二级分类
     * 📌 同级分类名称不能重复
     * 📌 仅支持两级，不能在二级下再创建子级
     *
     * @param dto 分类信息
     * @return 操作结果
     */
    @PostMapping("/add")
    @ApiOperation("新增分类")
    @AdminLog("新增分类")
    public Result<Void> addCategory(@Validated @RequestBody AddCategoryDTO dto) {
        adminCategoryService.addCategory(dto);
        return Result.success();
    }

    // =========================================================================
    // 3. 编辑分类
    // =========================================================================

    /**
     * 编辑分类
     *
     * 📌 可修改：名称、图标、排序、描述、是否显示
     * 📌 不允许修改 parentId（不可跨级移动）
     *
     * @param id  分类ID
     * @param dto 分类信息
     * @return 操作结果
     */
    @PutMapping("/update/{id}")
    @ApiOperation("编辑分类")
    @AdminLog("编辑分类")
    public Result<Void> updateCategory(
            @ApiParam("分类ID") @PathVariable Long id,
            @Validated @RequestBody AddCategoryDTO dto) {
        adminCategoryService.updateCategory(id, dto);
        return Result.success();
    }

    // =========================================================================
    // 4. 删除分类
    // =========================================================================

    /**
     * 删除分类
     *
     * 📌 删除前检查：该分类（及子分类）下是否有关联商品
     * 📌 有商品时返回错误提示，不允许删除
     * 📌 一级分类删除时级联逻辑删除其所有二级子分类
     * 📌 逻辑删除（设置 deleted=1）
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除分类")
    @AdminLog("删除分类")
    public Result<Void> deleteCategory(
            @ApiParam("分类ID") @PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return Result.success();
    }

    // =========================================================================
    // 5. 分类排序
    // =========================================================================

    /**
     * 批量排序
     *
     * 📌 前端拖拽排序后提交新的排序列表
     * 📌 批量更新所有分类的 sort 字段
     * 📌 sort 值越大越靠前
     *
     * @param dto 排序列表
     * @return 操作结果
     */
    @PostMapping("/sort")
    @ApiOperation("分类排序")
    @AdminLog("分类排序")
    public Result<Void> sortCategories(@Validated @RequestBody SortCategoryDTO dto) {
        adminCategoryService.sortCategories(dto);
        return Result.success();
    }
}
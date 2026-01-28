package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.service.AdminProductService;
import com.astronomy.mall.module.admin.vo.AdminProductVO;
import com.astronomy.mall.module.admin.vo.StockWarningVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.astronomy.mall.common.result.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 管理员-商品管理控制器
 *
 * 📌 功能清单:
 * 1. 商品列表查询(分页/搜索/筛选)
 * 2. 商品新增
 * 3. 商品编辑
 * 4. 商品上下架(批量)
 * 5. 库存调整
 * 6. 商品删除(逻辑删除)
 * 7. 库存预警列表
 *
 * 📌 权限说明:
 * 所有接口需要管理员权限 (role=1)
 * AdminInterceptor 会自动拦截验证
 */
@Slf4j
@Api(tags = "管理员-商品管理")
@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    /**
     * 1. 商品列表查询 (分页/搜索/筛选)
     */
    @ApiOperation("商品列表查询")
    @GetMapping("/list")
    public Result<Page<AdminProductVO>> list(@Validated ProductQueryDTO dto) {
        log.info("管理员查询商品列表: {}", dto);
        Page<AdminProductVO> page = adminProductService.getProductList(dto);
        return Result.success(page);
    }

    /**
     * 2. 商品详情查询 (用于编辑回显)
     */
    @ApiOperation("商品详情查询")
    @GetMapping("/detail/{id}")
    public Result<AdminProductVO> detail(@PathVariable Long id) {
        log.info("管理员查询商品详情, productId: {}", id);
        AdminProductVO product = adminProductService.getProductDetail(id);
        return Result.success(product);
    }

    /**
     * 3. 新增商品
     */
    @AdminLog("新增商品")
    @ApiOperation("新增商品")
    @PostMapping("/add")
    public Result<Void> add(@Validated @RequestBody ProductSaveDTO dto) {
        log.info("管理员新增商品: {}", dto.getProductName());
        adminProductService.addProduct(dto);
        return Result.success();
    }

    /**
     * 4. 编辑商品
     */
    @AdminLog("编辑商品")
    @ApiOperation("编辑商品")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody ProductSaveDTO dto) {
        log.info("管理员编辑商品, productId: {}, productName: {}", id, dto.getProductName());
        dto.setId(id);
        adminProductService.updateProduct(dto);
        return Result.success();
    }

    /**
     * 5. 商品上下架 (批量)
     */
    @AdminLog("商品上下架")
    @ApiOperation("商品上下架")
    @PostMapping("/status")
    public Result<Void> updateStatus(@Validated @RequestBody ProductStatusDTO dto) {
        log.info("管理员修改商品状态: productIds={}, status={}", dto.getProductIds(), dto.getStatus());
        adminProductService.updateStatus(dto);
        return Result.success();
    }

    /**
     * 6. 库存调整
     */
    @AdminLog("库存调整")
    @ApiOperation("库存调整")
    @PutMapping("/stock/{id}")
    public Result<Void> adjustStock(@PathVariable Long id, @Validated @RequestBody StockAdjustDTO dto) {
        log.info("管理员调整库存, productId: {}, adjustType: {}, quantity: {}",
                id, dto.getAdjustType(), dto.getQuantity());
        adminProductService.adjustStock(id, dto);
        return Result.success();
    }

    /**
     * 7. 删除商品 (逻辑删除)
     */
    @AdminLog("删除商品")
    @ApiOperation("删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("管理员删除商品, productId: {}", id);
        adminProductService.deleteProduct(id);
        return Result.success();
    }

    /**
     * 8. 库存预警列表
     */
    @ApiOperation("库存预警列表")
    @GetMapping("/stock-warning")
    public Result<List<StockWarningVO>> stockWarning() {
        log.info("管理员查询库存预警列表");
        List<StockWarningVO> list = adminProductService.getStockWarning();
        return Result.success(list);
    }
    /**
     * 9. 批量导入商品
     */
    @PostMapping("/import")
    @ApiOperation("批量导入商品")
    public Result<Map<String, Object>> importProducts(@RequestParam("file") MultipartFile file) {
        log.info("========== 商品批量导入 ==========");
        log.info("文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        Map<String, Object> result = adminProductService.importProducts(file);
        return Result.success(result);
    }

    /**
     * 10. 批量导出商品
     */
    @GetMapping("/export")
    @ApiOperation("批量导出商品")
    public void exportProducts(
            HttpServletResponse response,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status
    ) throws IOException {
        log.info("========== 商品批量导出 ==========");
        log.info("筛选条件: productName={}, categoryId={}, status={}",
                productName, categoryId, status);

        adminProductService.exportProducts(response, productName, categoryId, status);
    }

    /**
     * 11. 下载导入模板
     */
    @GetMapping("/download-template")
    @ApiOperation("下载导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        log.info("下载商品导入模板");
        adminProductService.downloadTemplate(response);
    }
}
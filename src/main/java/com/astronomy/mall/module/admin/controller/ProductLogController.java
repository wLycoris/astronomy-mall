package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.ProductLogQueryDTO;
import com.astronomy.mall.module.admin.service.ProductLogService;
import com.astronomy.mall.module.admin.vo.ProductLogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品日志管理
 *
 * 路径: com.astronomy.mall.module.admin.controller.ProductLogController
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/product-log")
@Api(tags = "后台管理-商品日志")
public class ProductLogController {

    @Autowired
    private ProductLogService productLogService;

    /**
     * 分页查询商品日志
     */
    @GetMapping("/list")
    @ApiOperation("商品日志列表")
    public Result<Page<ProductLogVO>> getProductLogList(ProductLogQueryDTO dto) {
        log.info("查询商品日志列表, pageNum: {}, pageSize: {}", dto.getPageNum(), dto.getPageSize());

        Page<ProductLogVO> page = productLogService.getProductLogList(dto);
        return Result.success(page);
    }

    /**
     * 查询单个商品的日志
     */
    @GetMapping("/product/{productId}")
    @ApiOperation("商品日志详情")
    public Result<Page<ProductLogVO>> getProductLog(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("查询商品日志, productId: {}", productId);

        ProductLogQueryDTO dto = new ProductLogQueryDTO();
        dto.setProductId(productId);
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);

        Page<ProductLogVO> page = productLogService.getProductLogList(dto);
        return Result.success(page);
    }
}
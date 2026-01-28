package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.entity.StockLog;
import com.astronomy.mall.module.admin.service.StockLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存日志管理
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/stock-log")
@Api(tags = "后台管理-库存日志")
public class StockLogController {

    @Autowired
    private StockLogService stockLogService;

    /**
     * 分页查询库存日志
     */
    @GetMapping("/list")
    @ApiOperation("库存日志列表")
    public Result<Page<StockLog>> getStockLogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long productId
    ) {
        log.info("查询库存日志, pageNum: {}, pageSize: {}, productId: {}",
                pageNum, pageSize, productId);

        Page<StockLog> page = stockLogService.getStockLogList(pageNum, pageSize, productId);
        return Result.success(page);
    }

    /**
     * 查询单个商品的库存日志
     */
    @GetMapping("/product/{productId}")
    @ApiOperation("商品库存日志")
    public Result<Page<StockLog>> getProductStockLog(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("查询商品库存日志, productId: {}", productId);

        Page<StockLog> page = stockLogService.getStockLogList(pageNum, pageSize, productId);
        return Result.success(page);
    }
}
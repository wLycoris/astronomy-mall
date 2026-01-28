package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.entity.StockLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 库存日志服务接口
 */
public interface StockLogService extends IService<StockLog> {

    /**
     * 分页查询库存日志
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param productId 商品ID(可选,为空则查询所有)
     * @return 分页结果
     */
    Page<StockLog> getStockLogList(Integer pageNum, Integer pageSize, Long productId);
}
package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.module.admin.entity.StockLog;
import com.astronomy.mall.module.admin.mapper.StockLogMapper;
import com.astronomy.mall.module.admin.service.StockLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 库存日志服务实现
 */
@Slf4j
@Service
public class StockLogServiceImpl extends ServiceImpl<StockLogMapper, StockLog>
        implements StockLogService {

    @Override
    public Page<StockLog> getStockLogList(Integer pageNum, Integer pageSize, Long productId) {

        Page<StockLog> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<StockLog> wrapper = new LambdaQueryWrapper<>();

        // 如果指定了商品ID,则只查询该商品的日志
        if (productId != null) {
            wrapper.eq(StockLog::getProductId, productId);
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(StockLog::getCreateTime);

        return this.page(page, wrapper);
    }
}
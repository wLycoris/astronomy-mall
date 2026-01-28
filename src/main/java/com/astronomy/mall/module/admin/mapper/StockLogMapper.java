package com.astronomy.mall.module.admin.mapper;

import com.astronomy.mall.module.admin.entity.StockLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存调整日志 Mapper
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
    // MyBatis-Plus 已经提供了基础的CRUD方法
    // 如果需要自定义SQL查询,可以在这里添加
}
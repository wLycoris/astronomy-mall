package com.astronomy.mall.module.admin.mapper;

import com.astronomy.mall.module.admin.entity.ProductLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品调整日志 Mapper
 *
 * 路径: com.astronomy.mall.module.admin.mapper.ProductLogMapper
 */
@Mapper
public interface ProductLogMapper extends BaseMapper<ProductLog> {
    // MyBatis-Plus 已经提供了基础的CRUD方法
    // 如果需要自定义SQL查询,可以在这里添加
}
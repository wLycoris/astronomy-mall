package com.astronomy.mall.module.product.mapper;

import com.astronomy.mall.module.product.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // MyBatis-Plus已提供基础CRUD,无需额外定义
}
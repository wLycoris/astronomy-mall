package com.astronomy.mall.module.aftersale.mapper;

import com.astronomy.mall.module.aftersale.entity.Recycling;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 二手回收申请 Mapper
 *
 * 📌 继承 BaseMapper 自动获得 CRUD 能力
 * 📌 表名: tb_recycling
 */
@Mapper
public interface RecyclingMapper extends BaseMapper<Recycling> {
}
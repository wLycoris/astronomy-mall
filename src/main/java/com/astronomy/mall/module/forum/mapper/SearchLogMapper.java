package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.SearchLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 搜索日志 Mapper
 *
 * 继承 BaseMapper<SearchLog> 获得通用 CRUD
 *
 * 📌 热搜统计查询（后续7.6实现时添加）:
 *   - selectHotKeywords: 按 keyword 分组统计近7天搜索次数，取Top10
 *
 * XML位置: resources/mapper/forum/SearchLogMapper.xml（后续需要时创建）
 */
@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

}

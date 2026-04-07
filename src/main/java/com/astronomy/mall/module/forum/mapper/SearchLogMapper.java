package com.astronomy.mall.module.forum.mapper;

import com.astronomy.mall.module.forum.entity.SearchLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索日志 Mapper
 *
 * 继承 BaseMapper<SearchLog> 获得通用 CRUD
 *
 * 📌 7.6 热搜统计:
 *   selectHotKeywords: 按 keyword 分组统计近7天搜索次数，取Top N
 */
@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

    /**
     * 热门搜索词 Top N（近7天，按搜索次数倒序）
     *
     * @param limit 返回条数（通常10）
     * @return 热搜关键词列表
     */
    @Select("SELECT keyword FROM tb_search_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY keyword " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT #{limit}")
    List<String> selectHotKeywords(@Param("limit") int limit);
}

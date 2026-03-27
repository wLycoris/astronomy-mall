package com.astronomy.mall.module.forum.service;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务接口
 *
 * 📌 实现计划:
 *   7.6: searchPosts / searchUsers / getHotSearch
 *
 * 📌 搜索策略:
 *   帖子: LIKE匹配 title+content, status=2(已发布), 按hot_score倒序
 *   用户: LIKE匹配 nickname+username, 最多20条
 *
 * 📌 热搜缓存:
 *   ConcurrentHashMap 内存缓存，1小时过期
 *   第16周引入Redis后可升级为Redis缓存
 *
 * 📌 搜索日志:
 *   @Async 异步写入 tb_search_log，不影响搜索性能
 */
public interface SearchService {

    /**
     * 搜索帖子/用户
     *
     * @param keyword  搜索关键词
     * @param type     搜索类型: post/user
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param userId   当前用户ID（异步记录搜索日志用，可为null）
     * @return 搜索结果 {list, total, pageNum, pageSize}
     */
    Map<String, Object> search(String keyword, String type, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 热门搜索词 Top10
     * 内存缓存1小时，过期后重新查DB统计
     *
     * @return 热搜关键词列表
     */
    List<String> getHotSearch();
}

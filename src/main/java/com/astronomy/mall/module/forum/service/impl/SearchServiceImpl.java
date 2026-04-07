package com.astronomy.mall.module.forum.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.module.forum.entity.SearchLog;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.forum.mapper.SearchLogMapper;
import com.astronomy.mall.module.forum.service.SearchService;
import com.astronomy.mall.module.forum.vo.PostVO;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 *
 * 📌 核心逻辑:
 *   7.6: 搜索帖子/用户(LIKE+分页) / 热搜统计(ConcurrentHashMap缓存1h)
 *
 * 📌 搜索策略:
 *   帖子: LIKE匹配 title+content, status=2(已发布), 按hot_score倒序
 *   用户: LIKE匹配 nickname+username, 最多20条
 *
 * 📌 热搜缓存:
 *   使用 volatile + 时间戳 内存缓存，避免引入 Redis 依赖
 *   缓存过期时间: 1小时（3600000ms）
 *   第16周推荐系统引入 Redis 后可升级
 *
 * 📌 搜索日志:
 *   @Async 异步写入 tb_search_log，不阻塞搜索响应
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private SearchLogMapper searchLogMapper;

    @Autowired
    private UserMapper userMapper;

    /** 热搜缓存 */
    private volatile List<String> hotSearchCache;

    /** 热搜缓存更新时间 */
    private volatile long hotSearchCacheTime = 0;

    /** 热搜缓存过期时间: 1小时 */
    private static final long HOT_SEARCH_CACHE_TTL = 3600000L;

    @Override
    public Map<String, Object> search(String keyword, String type, Integer pageNum, Integer pageSize, Long userId) {
        // 1. 参数校验
        if (keyword == null || keyword.trim().isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("list", Collections.emptyList());
            empty.put("total", 0L);
            empty.put("pageNum", pageNum);
            empty.put("pageSize", pageSize);
            return empty;
        }
        keyword = keyword.trim();
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        if (pageSize > 50) pageSize = 50;

        // 2. 异步记录搜索日志
        saveSearchLog(keyword, type, userId);

        // 3. 根据类型执行搜索
        if ("user".equals(type)) {
            return searchUsers(keyword, pageNum, pageSize);
        } else if ("topic".equals(type)) {
            return searchTopics(keyword);
        } else {
            return searchPosts(keyword, pageNum, pageSize);
        }
    }

    /**
     * 搜索帖子: LIKE匹配 title+content, status=2(已发布), 按hot_score倒序
     */
    private Map<String, Object> searchPosts(String keyword, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        // 使用PostMapper的XML查询（复用selectPostList的字段格式）
        List<Map<String, Object>> rows = postMapper.searchPosts("%" + keyword + "%", offset, pageSize);
        long total = postMapper.countSearchPosts("%" + keyword + "%");

        // 转换为PostVO列表（复用convertRowToPostVO的字段映射）
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>(row);
            // 处理 images JSON → coverImage + imageCount（使用fastjson，与PostServiceImpl一致）
            String imagesStr = row.get("images") != null ? row.get("images").toString() : null;
            if (StringUtils.hasText(imagesStr)) {
                try {
                    List<String> imageList = JSON.parseArray(imagesStr, String.class);
                    if (imageList != null && !imageList.isEmpty()) {
                        item.put("coverImage", imageList.get(0));
                        item.put("imageCount", imageList.size());
                        item.put("images", imageList);
                    }
                } catch (Exception e) {
                    log.warn("解析帖子图片JSON失败: {}", e.getMessage());
                }
            }
            // 处理 tags JSON（使用fastjson）
            String tagsStr = row.get("tags") != null ? row.get("tags").toString() : null;
            if (StringUtils.hasText(tagsStr)) {
                try {
                    List<String> tagList = JSON.parseArray(tagsStr, String.class);
                    if (tagList != null) {
                        item.put("tags", tagList);
                    }
                } catch (Exception e) {
                    log.warn("解析帖子标签JSON失败: {}", e.getMessage());
                }
            }
            item.remove("content"); // 列表不返回正文
            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 搜索用户: LIKE匹配 nickname+username, 按id倒序
     */
    private Map<String, Object> searchUsers(String keyword, Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> userPage = userMapper.selectPage(page,
                new LambdaQueryWrapper<User>()
                        .and(w -> w.like(User::getNickname, keyword)
                                .or()
                                .like(User::getUsername, keyword))
                        .eq(User::getStatus, 1)
                        .orderByDesc(User::getId));

        List<Map<String, Object>> list = userPage.getRecords().stream().map(u -> {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", u.getId());
            item.put("nickname", u.getNickname());
            item.put("username", u.getUsername());
            item.put("avatar", u.getAvatar());
            item.put("observationLevel", u.getObservationLevel());
            item.put("city", u.getCity());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", userPage.getTotal());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 搜索话题/标签: 从所有已发布帖子的 tags JSON 中提取匹配关键词的标签
     * 去重后返回，每个标签附带对应帖子数量
     */
    private Map<String, Object> searchTopics(String keyword) {
        // 查询所有已发布帖子的tags字段（非空的）
        List<Map<String, Object>> rows = postMapper.selectAllTags();
        // 遍历提取匹配的标签 + 统计每个标签的帖子数
        Map<String, Integer> tagCountMap = new LinkedHashMap<>();
        String kwLower = keyword.toLowerCase();
        for (Map<String, Object> row : rows) {
            String tagsStr = row.get("tags") != null ? row.get("tags").toString() : null;
            if (!StringUtils.hasText(tagsStr)) continue;
            try {
                List<String> tagList = JSON.parseArray(tagsStr, String.class);
                if (tagList == null) continue;
                // 对每个帖子的标签去重后统计
                Set<String> seen = new HashSet<>();
                for (String tag : tagList) {
                    if (tag != null && tag.toLowerCase().contains(kwLower) && seen.add(tag)) {
                        tagCountMap.merge(tag, 1, Integer::sum);
                    }
                }
            } catch (Exception e) {
                // 忽略解析失败的
            }
        }

        // 按帖子数量倒序排列
        List<Map<String, Object>> list = tagCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("tag", entry.getKey());
                    item.put("postCount", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", (long) list.size());
        result.put("pageNum", 1);
        result.put("pageSize", list.size());
        return result;
    }

    @Override
    public List<String> getHotSearch() {
        long now = System.currentTimeMillis();
        // 缓存未过期，直接返回
        if (hotSearchCache != null && (now - hotSearchCacheTime) < HOT_SEARCH_CACHE_TTL) {
            return hotSearchCache;
        }

        // 查询近7天搜索频次Top10
        List<String> hotList = searchLogMapper.selectHotKeywords(10);
        if (hotList == null) {
            hotList = Collections.emptyList();
        }

        // 更新缓存
        hotSearchCache = hotList;
        hotSearchCacheTime = now;

        return hotList;
    }

    /**
     * 异步写入搜索日志
     * 不影响搜索响应速度
     */
    @Async
    public void saveSearchLog(String keyword, String type, Long userId) {
        try {
            SearchLog searchLog = new SearchLog();
            searchLog.setKeyword(keyword);
            searchLog.setSearchType(type != null ? type : "post");
            searchLog.setUserId(userId);
            searchLogMapper.insert(searchLog);
        } catch (Exception e) {
            log.warn("异步保存搜索日志失败: {}", e.getMessage());
        }
    }
}

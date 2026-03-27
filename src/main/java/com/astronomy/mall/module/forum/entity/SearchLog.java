package com.astronomy.mall.module.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索日志实体类
 * 对应数据库表: tb_search_log
 *
 * 📌 用途:
 *   1. 记录用户搜索行为，用于热搜排行统计
 *   2. @Async 异步写入，不影响搜索性能
 *   3. 热搜缓存使用 ConcurrentHashMap（1小时过期），第16周可升级为Redis
 *
 * 📌 search_type:
 *   'post' = 搜索帖子
 *   'user' = 搜索用户
 */
@Data
@TableName("tb_search_log")
public class SearchLog {

    /** 搜索日志ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 搜索关键词 */
    private String keyword;

    /** 搜索用户ID（未登录可为null） */
    private Long userId;

    /** 搜索类型: post/user */
    private String searchType;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

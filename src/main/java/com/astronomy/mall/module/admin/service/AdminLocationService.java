package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.location.dto.SpotCreateDTO;
import com.astronomy.mall.module.location.dto.SpotUpdateDTO;

import java.util.Map;

/**
 * 管理员端 - 观测点管理服务接口
 * 6.5 后台观测点管理
 *
 * 提供5个方法:
 *   listSpots    分页+筛选查询观测点列表
 *   addSpot      新增观测点
 *   updateSpot   编辑观测点
 *   deleteSpot   软删除观测点
 *   getSpotStats 查询某观测点的签到统计
 */
public interface AdminLocationService {

    /**
     * 观测点分页列表（管理员）
     *
     * @param pageNum  页码，从1开始
     * @param pageSize 每页条数
     * @param province 省份筛选（可为null）
     * @param city     城市筛选（可为null）
     * @param keyword  关键词搜索观测点名称（可为null）
     * @return 包含 records 和 total 的分页结果
     */
    Map<String, Object> listSpots(Integer pageNum, Integer pageSize,
                                  String province, String city, String keyword);

    /**
     * 新增观测点
     *
     * @param dto 观测点创建参数
     */
    void addSpot(SpotCreateDTO dto);

    /**
     * 编辑观测点
     *
     * @param id  观测点ID
     * @param dto 观测点更新参数
     */
    void updateSpot(Long id, SpotUpdateDTO dto);

    /**
     * 删除观测点（逻辑删除，deleted=1）
     *
     * @param id 观测点ID
     */
    void deleteSpot(Long id);

    /**
     * 签到统计（总次数/近7日/近30日/TOP5用户）
     *
     * @param spotId 观测点ID
     * @return 统计数据 Map: totalCount, last7Days, last30Days, topUsers
     */
    Map<String, Object> getSpotStats(Long spotId);
}

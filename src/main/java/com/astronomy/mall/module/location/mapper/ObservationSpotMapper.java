package com.astronomy.mall.module.location.mapper;

import com.astronomy.mall.module.location.entity.ObservationSpot;
import com.astronomy.mall.module.location.vo.ObservationSpotVO;
import com.astronomy.mall.module.location.vo.SpotDetailVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 观测点 Mapper
 *
 * 继承 BaseMapper<ObservationSpot> 获得通用 CRUD
 *
 * 自定义方法说明:
 *   selectNearbySpots  - Haversine公式计算距离 + 省市/光污染多条件筛选 + 距离排序
 *   selectSpotDetail   - 查单个观测点详情，含 myScore（当前用户评分）和签到统计
 *
 * XML位置: resources/mapper/xml/ObservationSpotMapper.xml
 */
@Mapper
public interface ObservationSpotMapper extends BaseMapper<ObservationSpot> {

    /**
     * 查询附近观测点（Haversine公式计算距离，按距离升序排列）
     *
     * @param longitude         查询中心经度
     * @param latitude          查询中心纬度
     * @param radius            搜索半径（km，默认100）
     * @param limit             返回条数上限（默认20）
     * @param province          省份筛选（可为null，null时不筛选）
     * @param city              城市筛选（可为null，null时不筛选）
     * @param maxLightPollution Bortle等级上限（可为null，null时不筛选；1=最暗，9=最亮，传3表示只看≤3级暗天）
     * @param currentUserId     当前登录用户ID（用于查询 myScore，未登录传null）
     * @return 按距离升序的观测点列表
     */
    List<ObservationSpotVO> selectNearbySpots(
            @Param("longitude") Double longitude,
            @Param("latitude") Double latitude,
            @Param("radius") Integer radius,
            @Param("limit") Integer limit,
            @Param("province") String province,
            @Param("city") String city,
            @Param("maxLightPollution") Integer maxLightPollution,
            @Param("currentUserId") Long currentUserId
    );

    /**
     * 查询单个观测点详情
     * 含:
     *   - 完整描述（full_description）
     *   - 图片列表（images JSON字符串，Service层解析）
     *   - 当前用户评分 myScore（currentUserId为null时返回null）
     *   - 今日签到人数 todayCheckinCount
     *   - 累计签到总数 totalCheckinCount
     *
     * @param spotId        观测点ID
     * @param currentUserId 当前登录用户ID（未登录传null）
     * @return 观测点详情VO（找不到返回null）
     */
    SpotDetailVO selectSpotDetail(
            @Param("spotId") Long spotId,
            @Param("currentUserId") Long currentUserId
    );
}
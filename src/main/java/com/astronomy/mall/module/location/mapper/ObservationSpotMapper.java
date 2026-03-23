package com.astronomy.mall.module.location.mapper;

import com.astronomy.mall.module.location.entity.ObservationSpot;
import com.astronomy.mall.module.location.vo.ObservationSpotVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 观测点Mapper
 * 对应表: tb_observation_spot
 *
 * 📌 6.0 骨架类，业务SQL在 6.1~6.5 各节填充
 *    @Update 注解方法在此处定义，无需XML
 */
public interface ObservationSpotMapper extends BaseMapper<ObservationSpot> {

    /**
     * 更新观测点综合评分和评分人数
     * 在 SpotRatingMapper.insertRating() 成功后调用
     *
     * @param spotId     观测点ID
     * @param avgRating  新的平均分（由Service层计算）
     * @param ratingCount 新的评分人数
     *
     * 📌 TODO 6.1: 由 LocationServiceImpl.rateSpot() 调用
     */
    @Update("UPDATE tb_observation_spot SET rating=#{avgRating}, rating_count=#{ratingCount} " +
            "WHERE id=#{spotId} AND deleted=0")
    void updateRating(@Param("spotId") Long spotId,
                      @Param("avgRating") BigDecimal avgRating,
                      @Param("ratingCount") int ratingCount);

    /**
     * 签到成功后，观测点总签到次数+1
     *
     * 📌 TODO 6.3: 由 LocationServiceImpl.checkin() 调用
     */
    @Update("UPDATE tb_observation_spot SET checkin_count=checkin_count+1 WHERE id=#{spotId} AND deleted=0")
    void incrCheckinCount(@Param("spotId") Long spotId);

    /**
     * 观测点列表查询（带筛选）
     * SQL 在 ObservationSpotMapper.xml 中实现
     *
     * 📌 TODO 6.1: 填充XML中的 SQL
     *
     * @param province             省份（可为null）
     * @param city                 城市（可为null）
     * @param maxLightPollution    最大光污染等级（可为null）
     * @return 观测点VO列表
     */
    List<ObservationSpotVO> listSpots(@Param("province") String province,
                                      @Param("city") String city,
                                      @Param("maxLightPollution") Integer maxLightPollution);
}
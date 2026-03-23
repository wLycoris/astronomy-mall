package com.astronomy.mall.module.location.mapper;

import com.astronomy.mall.module.location.entity.SpotRating;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 观测点评分记录Mapper
 * 对应表: tb_spot_rating
 *
 * 📌 6.0 骨架类，业务SQL在 6.1 节填充
 */
public interface SpotRatingMapper extends BaseMapper<SpotRating> {

    /**
     * 查询某观测点的平均评分
     * 在新增评分后调用，用于更新 tb_observation_spot.rating
     *
     * 📌 TODO 6.1: 由 LocationServiceImpl.rateSpot() 调用
     *
     * @param spotId 观测点ID
     * @return 平均分（保留2位小数）
     */
    @Select("SELECT ROUND(AVG(score), 2) FROM tb_spot_rating WHERE spot_id=#{spotId}")
    BigDecimal calcAvgRating(@Param("spotId") Long spotId);

    /**
     * 查询某观测点的评分人数
     *
     * @param spotId 观测点ID
     * @return 评分人数
     */
    @Select("SELECT COUNT(*) FROM tb_spot_rating WHERE spot_id=#{spotId}")
    int countRating(@Param("spotId") Long spotId);

    /**
     * 查询用户对某观测点的已有评分（防重复用）
     *
     * @param userId 用户ID
     * @param spotId 观测点ID
     * @return 已有评分分值（null=未评分过）
     */
    @Select("SELECT score FROM tb_spot_rating WHERE user_id=#{userId} AND spot_id=#{spotId}")
    Integer getUserScore(@Param("userId") Long userId, @Param("spotId") Long spotId);
}
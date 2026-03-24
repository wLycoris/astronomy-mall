package com.astronomy.mall.module.location.mapper;

import com.astronomy.mall.module.location.entity.SpotRating;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 观测点评分 Mapper
 *
 * 继承 BaseMapper<SpotRating> 获得通用 CRUD（insert/selectById 等）
 *
 * UNIQUE 约束: tb_spot_rating.uk_user_spot(user_id, spot_id)
 *   - 数据库层面保证一人一地只能评分一次
 *   - 业务层（LocationServiceImpl）会提前查询，给出友好的中文提示
 *
 * 更新评分均值: 提交评分后调用 updateSpotRating()
 *   - 直接在 tb_observation_spot 上执行 AVG(score) 子查询更新
 *   - 比在 Java 层做聚合再回写更可靠（避免并发问题）
 *
 * XML位置: resources/mapper/xml/SpotRatingMapper.xml
 */
@Mapper
public interface SpotRatingMapper extends BaseMapper<SpotRating> {

    /**
     * 查询某用户对某观测点的评分（用于提前判断是否已评分）
     *
     * @param userId 用户ID
     * @param spotId 观测点ID
     * @return 评分（1-5），未评分返回 null
     */
    @Select("SELECT score FROM tb_spot_rating " +
            "WHERE user_id = #{userId} AND spot_id = #{spotId} LIMIT 1")
    Integer selectUserScore(@Param("userId") Long userId, @Param("spotId") Long spotId);

    /**
     * 重新计算并更新观测点的平均分和评分人数
     *
     * 触发时机: 每次 tb_spot_rating 新增/修改后调用
     * SQL逻辑:
     *   - rating     = ROUND(AVG(score), 1)  保留1位小数
     *   - rating_count = COUNT(1)
     *   - 若无评分记录则置为 0.0 / 0
     *
     * @param spotId 观测点ID
     */
    void updateSpotRating(@Param("spotId") Long spotId);
}
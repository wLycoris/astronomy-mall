package com.astronomy.mall.module.product.mapper;

import com.astronomy.mall.module.product.entity.Review;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 获取商品评价统计
     */
    @Select("SELECT " +
            "COUNT(*) AS reviewCount, " +
            "IFNULL(AVG(rating), 0) AS avgRating, " +
            "IFNULL(SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 0) AS goodRate " +
            "FROM tb_review " +
            "WHERE product_id = #{productId} AND deleted = 0")
    Map<String, Object> getReviewStatistics(@Param("productId") Long productId);
}
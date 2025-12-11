package com.astronomy.mall.module.product.mapper;

import com.astronomy.mall.module.product.entity.Review;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 评价Mapper接口(修复版)
 *
 * ✅ 核心修复:
 * 1. checkOrderReviewed 改名为 checkProductReviewed
 * 2. 新增 productId 参数
 * 3. 检查条件改为: 订单ID + 商品ID + 用户ID
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select({
            "<script>",
            "SELECT ",
            "  COUNT(*) AS reviewCount,",
            "  IFNULL(AVG(rating), 0) AS avgRating,",
            "  IFNULL(SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 0) AS goodRate,",
            "  IFNULL(SUM(like_count), 0) AS totalLikes,",
            "  SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) AS fiveStar,",
            "  SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) AS fourStar,",
            "  SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) AS threeStar,",
            "  SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) AS twoStar,",
            "  SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) AS oneStar,",
            "  SUM(CASE WHEN images IS NOT NULL AND images != '' THEN 1 ELSE 0 END) AS hasImagesCount ",
            "FROM tb_review ",
            "WHERE product_id = #{productId} AND deleted = 0 AND status = 1",
            "</script>"
    })
    Map<String, Object> getReviewStatistics(@Param("productId") Long productId);

    @Select({
            "<script>",
            "SELECT ",
            "  r.id, r.user_id, r.product_id, r.order_id, r.rating, r.content, ",
            "  r.images, r.like_count, r.reply, r.reply_time, r.is_anonymous, ",
            "  r.status, r.create_time, r.update_time,",
            "  u.username, u.nickname, u.avatar,",
            "  p.product_name AS productName, p.main_image AS productImage ",
            "FROM tb_review r ",
            "LEFT JOIN tb_user u ON r.user_id = u.id ",
            "LEFT JOIN tb_product p ON r.product_id = p.id ",
            "WHERE r.product_id = #{productId} AND r.deleted = 0 AND r.status = 1 ",
            "<if test='rating != null and rating > 0'>AND r.rating = #{rating} </if>",
            "<if test='hasImages != null and hasImages == 1'>",
            "  AND r.images IS NOT NULL AND r.images != '' ",
            "</if>",
            "<choose>",
            "  <when test='sortType == 2'>ORDER BY r.like_count DESC, r.create_time DESC</when>",
            "  <when test='sortType == 3'>ORDER BY r.rating DESC, r.create_time DESC</when>",
            "  <when test='sortType == 4'>ORDER BY r.rating ASC, r.create_time DESC</when>",
            "  <otherwise>ORDER BY r.create_time DESC</otherwise>",
            "</choose> ",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"
    })
    List<Map<String, Object>> getReviewList(
            @Param("productId") Long productId,
            @Param("rating") Integer rating,
            @Param("hasImages") Integer hasImages,
            @Param("sortType") Integer sortType,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM tb_review ",
            "WHERE product_id = #{productId} AND deleted = 0 AND status = 1 ",
            "<if test='rating != null and rating > 0'>AND rating = #{rating} </if>",
            "<if test='hasImages != null and hasImages == 1'>",
            "  AND images IS NOT NULL AND images != '' ",
            "</if>",
            "</script>"
    })
    Integer getReviewCount(
            @Param("productId") Long productId,
            @Param("rating") Integer rating,
            @Param("hasImages") Integer hasImages
    );

    /**
     * ✅ 核心修复: 检查指定商品是否已评价
     *
     * 旧方法名: checkOrderReviewed (只检查订单)
     * 新方法名: checkProductReviewed (检查订单+商品)
     *
     * 修复逻辑:
     * - 旧: WHERE order_id = #{orderId} AND user_id = #{userId}
     * - 新: WHERE order_id = #{orderId} AND product_id = #{productId} AND user_id = #{userId}
     *
     * 使用场景:
     * - 发布评价前检查该商品是否已评价
     * - 允许同一订单的不同商品分别评价
     *
     * @param orderId 订单ID
     * @param productId 商品ID (新增参数)
     * @param userId 用户ID
     * @return 评价数量(0=未评价, >0=已评价)
     */
    @Select("SELECT COUNT(*) FROM tb_review " +
            "WHERE order_id = #{orderId} " +
            "AND product_id = #{productId} " +
            "AND user_id = #{userId} " +
            "AND deleted = 0")
    Integer checkProductReviewed(
            @Param("orderId") Long orderId,
            @Param("productId") Long productId,
            @Param("userId") Long userId
    );

    @Update("UPDATE tb_review SET like_count = like_count + 1 WHERE id = #{reviewId}")
    int increaseLikeCount(@Param("reviewId") Long reviewId);

    @Update("UPDATE tb_review SET like_count = like_count - 1 " +
            "WHERE id = #{reviewId} AND like_count > 0")
    int decreaseLikeCount(@Param("reviewId") Long reviewId);

    @Select({
            "<script>",
            "SELECT ",
            "  r.id, ",
            "  r.product_id, ",
            "  r.order_id, ",
            "  r.rating, ",
            "  r.content, ",
            "  r.images, ",
            "  r.like_count, ",
            "  r.reply, ",
            "  r.reply_time, ",
            "  r.create_time, ",
            "  p.product_name AS productName, ",
            "  p.main_image AS productImage, ",
            "  p.price AS productPrice ",
            "FROM tb_review r ",
            "LEFT JOIN tb_product p ON r.product_id = p.id ",
            "WHERE r.user_id = #{userId} AND r.deleted = 0 ",
            "ORDER BY r.create_time DESC ",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"
    })
    List<Map<String, Object>> getUserReviewList(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    @Select("SELECT COUNT(*) FROM tb_review WHERE user_id = #{userId} AND deleted = 0")
    Integer getUserReviewCount(@Param("userId") Long userId);
}
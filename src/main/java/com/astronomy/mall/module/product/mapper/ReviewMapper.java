package com.astronomy.mall.module.product.mapper;

import com.astronomy.mall.module.product.entity.Review;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    // ============================================
    // 评价统计
    // ============================================

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

    // ============================================
    // 商品详情页评价列表（高级版，支持筛选+排序）
    // ============================================

    @Select({
            "<script>",
            "SELECT ",
            "  r.id, r.user_id, r.product_id, r.order_id, r.rating, r.content, ",
            "  r.images, r.like_count, r.reply, r.reply_time, r.is_anonymous, ",
            "  r.status, r.create_time, r.update_time,",
            "  r.is_top, r.top_time, ",
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
            "  <when test='sortType == 2'>ORDER BY r.is_top DESC, r.top_time DESC, r.like_count DESC, r.create_time DESC</when>",
            "  <when test='sortType == 3'>ORDER BY r.is_top DESC, r.top_time DESC, r.rating DESC, r.create_time DESC</when>",
            "  <when test='sortType == 4'>ORDER BY r.is_top DESC, r.top_time DESC, r.rating ASC, r.create_time DESC</when>",
            "  <otherwise>ORDER BY r.is_top DESC, r.top_time DESC, r.create_time DESC</otherwise>",
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

    // ============================================
    // 发布评价前：检查是否已评价过该商品
    // deleted=0：用户自己删了可以重发
    // status=0：被管理员删了也不能重发
    // 两个条件都要检查
    // ============================================

    @Select("SELECT COUNT(*) FROM tb_review " +
            "WHERE order_id = #{orderId} " +
            "AND product_id = #{productId} " +
            "AND user_id = #{userId} " +
            "AND (deleted = 0 OR status = 0)")
    Integer checkProductReviewed(
            @Param("orderId") Long orderId,
            @Param("productId") Long productId,
            @Param("userId") Long userId
    );

    // ============================================
    // 点赞
    // ============================================

    @Update("UPDATE tb_review SET like_count = like_count + 1 WHERE id = #{reviewId}")
    int increaseLikeCount(@Param("reviewId") Long reviewId);

    @Update("UPDATE tb_review SET like_count = like_count - 1 " +
            "WHERE id = #{reviewId} AND like_count > 0")
    int decreaseLikeCount(@Param("reviewId") Long reviewId);

    // ============================================
    // 我的评价列表
    // deleted=0：只查未被用户自己删除的（@TableLogic管不到手写SQL，手动加）
    // status 不过滤：status=0(管理员删除) 也要展示，前端显示"已被删除"提示
    // ============================================

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
            "  r.status, ",
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

    // ============================================
    // 举报功能
    // ============================================

    @Select("SELECT COUNT(*) FROM tb_review_report " +
            "WHERE review_id = #{reviewId} AND user_id = #{userId}")
    Integer checkUserReported(
            @Param("reviewId") Long reviewId,
            @Param("userId") Long userId
    );

    @Insert("INSERT INTO tb_review_report(review_id, user_id, reason, create_time) " +
            "VALUES(#{reviewId}, #{userId}, #{reason}, NOW())")
    int insertReport(
            @Param("reviewId") Long reviewId,
            @Param("userId") Long userId,
            @Param("reason") String reason
    );

    @Update("UPDATE tb_review " +
            "SET report_count = report_count + 1, " +
            "    status = CASE WHEN report_count + 1 >= 3 AND status = 1 THEN 2 ELSE status END " +
            "WHERE id = #{reviewId}")
    int incrementReportCount(@Param("reviewId") Long reviewId);
}
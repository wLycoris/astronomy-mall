package com.astronomy.mall.module.product.mapper;

import com.astronomy.mall.module.product.entity.Review;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 评价Mapper接口
 *
 * 功能说明:
 * 1. 提供评价相关的数据库操作方法
 * 2. 继承MyBatis-Plus的BaseMapper,获得基础CRUD能力
 * 3. 自定义复杂查询方法
 *
 * @author 天文商城开发团队
 * @since 2025-11-14
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 获取商品评价统计信息
     *
     * 统计内容:
     * 1. reviewCount: 评价总数
     * 2. avgRating: 平均评分
     * 3. goodRate: 好评率(4-5星占比)
     * 4. totalLikes: 总点赞数
     * 5. fiveStar ~ oneStar: 各星级评价数
     * 6. hasImagesCount: 有图评价数
     *
     * 性能优化:
     * - 使用一次SQL查询获取所有统计数据
     * - 避免多次查询数据库
     * - 使用IFNULL防止除零错误
     *
     * @param productId 商品ID
     * @return 统计数据Map
     */
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

    /**
     * 分页查询评价列表(高级版)
     *
     * 功能特性:
     * 1. 支持星级筛选(rating参数)
     * 2. 支持有图筛选(hasImages参数)
     * 3. 支持多种排序方式(sortType参数)
     * 4. 关联查询用户信息和商品信息
     * 5. 支持匿名评价处理
     *
     * 排序方式:
     * - sortType=1: 最新评价(按创建时间降序)
     * - sortType=2: 点赞最多(按点赞数降序)
     * - sortType=3: 评分最高(按评分降序)
     * - sortType=4: 评分最低(按评分升序)
     *
     * @param productId 商品ID
     * @param rating 星级筛选(0=全部, 1-5=对应星级)
     * @param hasImages 是否有图(0=全部, 1=仅看有图)
     * @param sortType 排序方式(1-4)
     * @param offset 分页偏移量
     * @param pageSize 每页数量
     * @return 评价列表
     */
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
            // 动态SQL: 星级筛选
            "<if test='rating != null and rating > 0'>AND r.rating = #{rating} </if>",
            // 动态SQL: 有图筛选
            "<if test='hasImages != null and hasImages == 1'>",
            "  AND r.images IS NOT NULL AND r.images != '' ",
            "</if>",
            // 动态SQL: 排序方式
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

    /**
     * 查询评价总数(用于分页)
     *
     * 功能说明:
     * - 配合getReviewList使用
     * - 支持相同的筛选条件
     * - 用于计算总页数
     *
     * @param productId 商品ID
     * @param rating 星级筛选
     * @param hasImages 是否有图
     * @return 评价总数
     */
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
     * 检查订单是否已评价
     *
     * 业务规则:
     * - 一个订单只能评价一次
     * - 防止重复评价
     *
     * 使用场景:
     * - 发布评价前检查
     * - 订单列表显示"已评价"标签
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 评价数量(0=未评价, >0=已评价)
     */
    @Select("SELECT COUNT(*) FROM tb_review " +
            "WHERE order_id = #{orderId} AND user_id = #{userId} AND deleted = 0")
    Integer checkOrderReviewed(@Param("orderId") Long orderId, @Param("userId") Long userId);

    /**
     * 点赞数+1
     *
     * 业务场景:
     * - 用户点赞评价时调用
     * - 使用MySQL的自增操作,保证线程安全
     *
     * @param reviewId 评价ID
     * @return 影响行数
     */
    @Update("UPDATE tb_review SET like_count = like_count + 1 WHERE id = #{reviewId}")
    int increaseLikeCount(@Param("reviewId") Long reviewId);

    /**
     * 点赞数-1
     *
     * 业务场景:
     * - 用户取消点赞时调用
     * - 使用MySQL的自减操作,保证线程安全
     * - 添加like_count > 0条件,防止出现负数
     *
     * @param reviewId 评价ID
     * @return 影响行数
     */
    @Update("UPDATE tb_review SET like_count = like_count - 1 " +
            "WHERE id = #{reviewId} AND like_count > 0")
    int decreaseLikeCount(@Param("reviewId") Long reviewId);

    /**
     * 获取用户的评价列表
     *
     * 功能说明:
     * - 查询指定用户的所有评价
     * - 用于"我的评价"页面
     * - 关联查询商品信息
     * - 按创建时间降序排列
     *
     * @param userId 用户ID
     * @param offset 分页偏移量
     * @param pageSize 每页数量
     * @return 用户评价列表
     */
    @Select({
            "<script>",
            "SELECT r.id, r.product_id, r.order_id, r.rating, r.content, ",
            "  r.images, r.like_count, r.reply, r.reply_time, r.create_time, ",
            "  p.product_name AS productName, p.main_image AS productImage ",
            "FROM tb_review r ",
            "LEFT JOIN tb_product p ON r.product_id = p.id ",
            "WHERE r.user_id = #{userId} AND r.deleted = 0 ",
            "ORDER BY r.create_time DESC LIMIT #{offset}, #{pageSize}",
            "</script>"
    })
    List<Map<String, Object>> getUserReviewList(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 获取用户评价总数
     *
     * 功能说明:
     * - 配合getUserReviewList使用
     * - 用于"我的评价"页面的分页
     *
     * @param userId 用户ID
     * @return 评价总数
     */
    @Select("SELECT COUNT(*) FROM tb_review WHERE user_id = #{userId} AND deleted = 0")
    Integer getUserReviewCount(@Param("userId") Long userId);
}
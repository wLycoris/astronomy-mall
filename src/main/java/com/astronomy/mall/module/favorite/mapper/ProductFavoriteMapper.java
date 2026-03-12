package com.astronomy.mall.module.favorite.mapper;

import com.astronomy.mall.module.favorite.entity.ProductFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 商品收藏 Mapper
 *
 * 📌 路径: com.astronomy.mall.module.favorite.mapper.ProductFavoriteMapper
 * 📌 继承 BaseMapper，自动提供 insert/delete/update/selectById 等基础方法
 *
 * 自定义查询：
 *   1. selectFavoriteList   - 收藏列表（LEFT JOIN 商品表，获取实时状态）
 *   2. countFavoriteByUser  - 统计用户收藏数（供 UserOverview 等处使用）
 *   3. selectFavoriteUserIds - 收藏了某商品的所有用户ID（供上架/降价通知用）
 */
@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavorite> {

    /**
     * 查询用户收藏列表（分页）
     *
     * 📌 LEFT JOIN tb_product 获取实时商品信息:
     *   - 商品存在且上架(status=1) → isOffShelf = false，取实时价格
     *   - 商品不存在(deleted=1)或已下架(status=0) → isOffShelf = true
     *
     * @param page   分页参数（MyBatis-Plus 自动处理 LIMIT/OFFSET）
     * @param userId 用户ID
     * @return 收藏列表 VO
     */
    @Select({
            "<script>",
            "SELECT ",
            "  f.id,",
            "  f.product_id AS productId,",
            "  COALESCE(p.main_image, f.product_image) AS productImage,",
            "  COALESCE(p.product_name, f.product_name) AS productName,",
            "  p.price AS currentPrice,",
            "  f.product_price AS favoritePrice,",
            "  f.create_time AS createTime,",
            "  CASE WHEN p.id IS NULL OR p.status = 0 THEN 1 ELSE 0 END AS isOffShelf,",
            "  CASE WHEN p.price IS NOT NULL AND p.price &lt; f.product_price THEN 1 ELSE 0 END AS isPriceDown,",
            "  CASE WHEN p.price IS NOT NULL AND p.price &gt; f.product_price THEN 1 ELSE 0 END AS isPriceUp",
            "FROM tb_product_favorite f",
            "LEFT JOIN tb_product p ON f.product_id = p.id AND p.deleted = 0",
            "WHERE f.user_id = #{userId}",
            "ORDER BY f.create_time DESC",
            "</script>"
    })
    List<java.util.Map<String, Object>> selectFavoriteList(Page<?> page, @Param("userId") Long userId);

    /**
     * 统计用户收藏总数（不分页，用于概览页等）
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Select("SELECT COUNT(*) FROM tb_product_favorite WHERE user_id = #{userId}")
    int countFavoriteByUser(@Param("userId") Long userId);

    /**
     * 查询收藏了指定商品的所有用户ID
     *
     * 📌 用途：
     *   - 商品上架时，通知所有收藏了该商品的用户
     *   - 商品降价时，通知所有收藏了该商品的用户
     *
     * @param productId 商品ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM tb_product_favorite WHERE product_id = #{productId}")
    List<Long> selectFavoriteUserIds(@Param("productId") Long productId);
}
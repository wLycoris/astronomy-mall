package com.astronomy.mall.module.favorite.service;

import com.astronomy.mall.module.favorite.vo.FavoriteVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 商品收藏 Service 接口
 *
 * 📌 路径: com.astronomy.mall.module.favorite.service.FavoriteService
 *
 * 提供以下能力:
 *   1. toggleFavorite   - 收藏/取消收藏（幂等切换）
 *   2. getFavoriteList  - 我的收藏列表（分页）
 *   3. checkFavorite    - 查询是否已收藏
 *   4. removeFavorite   - 取消收藏（主动删除）
 */
public interface FavoriteService {

    /**
     * 收藏 / 取消收藏（幂等切换）
     *
     * 📌 逻辑:
     *   - 已收藏 → 取消收藏（DELETE）
     *   - 未收藏 → 添加收藏（INSERT，同时冗余存商品基本信息）
     *
     * @param userId    当前用户ID
     * @param productId 商品ID
     * @return true=当前已收藏, false=当前已取消收藏
     */
    boolean toggleFavorite(Long userId, Long productId);

    /**
     * 获取我的收藏列表（分页）
     *
     * @param userId   当前用户ID
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认12）
     * @return 分页收藏列表（含商品实时状态、是否下架、是否降价）
     */
    Page<FavoriteVO> getFavoriteList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询用户是否已收藏某商品
     *
     * 📌 商品详情页进入时调用，同步收藏按钮状态（红心/灰心）
     *
     * @param userId    当前用户ID
     * @param productId 商品ID
     * @return true=已收藏, false=未收藏
     */
    boolean checkFavorite(Long userId, Long productId);

    /**
     * 取消收藏（主动删除，与 toggle 等效，但语义更明确）
     *
     * @param userId    当前用户ID
     * @param productId 商品ID
     */
    void removeFavorite(Long userId, Long productId);
}
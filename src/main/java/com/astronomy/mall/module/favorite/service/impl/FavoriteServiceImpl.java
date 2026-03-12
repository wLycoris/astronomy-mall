package com.astronomy.mall.module.favorite.service.impl;

import cn.hutool.core.convert.Convert;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.favorite.entity.ProductFavorite;
import com.astronomy.mall.module.favorite.mapper.ProductFavoriteMapper;
import com.astronomy.mall.module.favorite.service.FavoriteService;
import com.astronomy.mall.module.favorite.vo.FavoriteVO;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品收藏 ServiceImpl
 *
 * 📌 路径: com.astronomy.mall.module.favorite.service.impl.FavoriteServiceImpl
 *
 * 技术要点:
 *   1. toggleFavorite: 先查再判断，NOT 用 insertOrUpdate 保证幂等
 *   2. getFavoriteList: 用 @Select LEFT JOIN 获取实时状态，性能优于多次查询
 *   3. 冗余字段: 收藏时快照商品名/主图/价格，防止商品删除后收藏列表报错
 */
@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private ProductFavoriteMapper productFavoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    // =============================
    // 收藏/取消收藏（幂等切换）
    // =============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long userId, Long productId) {
        // 1. 查询是否已收藏
        ProductFavorite existing = getExistingFavorite(userId, productId);

        if (existing != null) {
            // 2. 已收藏 → 取消收藏
            productFavoriteMapper.deleteById(existing.getId());
            log.info("[收藏] 用户取消收藏, userId={}, productId={}", userId, productId);
            return false;  // false = 当前已取消
        }

        // 3. 未收藏 → 查询商品信息（含下架商品也允许收藏，方便比价）
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 4. 插入收藏记录，冗余存商品基本信息
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setProductName(product.getProductName());    // 冗余商品名
        favorite.setProductPrice(product.getPrice());          // 冗余收藏时价格
        favorite.setProductImage(product.getMainImage());      // 冗余主图
        productFavoriteMapper.insert(favorite);

        log.info("[收藏] 用户添加收藏, userId={}, productId={}, productName={}",
                userId, productId, product.getProductName());
        return true;  // true = 当前已收藏
    }

    // =============================
    // 我的收藏列表（分页）
    // =============================

    @Override
    public Page<FavoriteVO> getFavoriteList(Long userId, Integer pageNum, Integer pageSize) {
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? 12 : pageSize;

        Page<Object> pageParam = new Page<>(pageNum, pageSize);

        // 执行联表查询
        List<Map<String, Object>> rawList = productFavoriteMapper.selectFavoriteList(pageParam, userId);

        // Map → FavoriteVO 转换
        List<FavoriteVO> voList = new ArrayList<>();
        for (Map<String, Object> row : rawList) {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(Convert.toLong(row.get("id")));
            vo.setProductId(Convert.toLong(row.get("productId")));
            vo.setProductImage(Convert.toStr(row.get("productImage")));
            vo.setProductName(Convert.toStr(row.get("productName")));

            // 当前价格（商品下架则为 null）
            Object currentPriceObj = row.get("currentPrice");
            if (currentPriceObj != null) {
                vo.setCurrentPrice(Convert.toBigDecimal(currentPriceObj));
            }

            // 收藏时价格
            Object favoritePriceObj = row.get("favoritePrice");
            if (favoritePriceObj != null) {
                vo.setFavoritePrice(Convert.toBigDecimal(favoritePriceObj));
            }

            // 是否下架（0=正常, 1=下架）
            Integer isOffShelfInt = Convert.toInt(row.get("isOffShelf"), 0);
            vo.setIsOffShelf(isOffShelfInt == 1);

            // 是否降价（0=未降价, 1=降价）
            Integer isPriceDownInt = Convert.toInt(row.get("isPriceDown"), 0);
            vo.setIsPriceDown(isPriceDownInt == 1);

            // 是否涨价（0=未涨价, 1=涨价）
            Integer isPriceUpInt = Convert.toInt(row.get("isPriceUp"), 0);
            vo.setIsPriceUp(isPriceUpInt == 1);

            // 收藏时间
            Object createTimeObj = row.get("createTime");
            if (createTimeObj instanceof java.time.LocalDateTime) {
                vo.setCreateTime((java.time.LocalDateTime) createTimeObj);
            }

            voList.add(vo);
        }

        // 组装分页结果
        Page<FavoriteVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotal(pageParam.getTotal());
        return result;
    }

    // =============================
    // 查询是否已收藏
    // =============================

    @Override
    public boolean checkFavorite(Long userId, Long productId) {
        return getExistingFavorite(userId, productId) != null;
    }

    // =============================
    // 取消收藏（主动删除）
    // =============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long userId, Long productId) {
        int deleted = productFavoriteMapper.delete(
                new LambdaQueryWrapper<ProductFavorite>()
                        .eq(ProductFavorite::getUserId, userId)
                        .eq(ProductFavorite::getProductId, productId)
        );
        if (deleted == 0) {
            throw new BusinessException("收藏记录不存在或已取消");
        }
        log.info("[收藏] 取消收藏成功, userId={}, productId={}", userId, productId);
    }

    // =============================
    // 私有辅助方法
    // =============================

    /**
     * 查询现有收藏记录（用于幂等判断）
     */
    private ProductFavorite getExistingFavorite(Long userId, Long productId) {
        return productFavoriteMapper.selectOne(
                new LambdaQueryWrapper<ProductFavorite>()
                        .eq(ProductFavorite::getUserId, userId)
                        .eq(ProductFavorite::getProductId, productId)
        );
    }
}
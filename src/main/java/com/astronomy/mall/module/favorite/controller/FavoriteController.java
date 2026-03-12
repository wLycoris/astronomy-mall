package com.astronomy.mall.module.favorite.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.favorite.service.FavoriteService;
import com.astronomy.mall.module.favorite.vo.FavoriteVO;
import com.astronomy.mall.utils.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品收藏 Controller
 *
 * 📌 路径: com.astronomy.mall.module.favorite.controller.FavoriteController
 * 📌 基础路径: /api/favorite
 * 📌 所有接口需要登录（由 JwtInterceptor 拦截）
 *
 * 接口列表 (4个):
 *   POST   /api/favorite/toggle/{productId}  - 收藏/取消（幂等切换）
 *   GET    /api/favorite/list                - 我的收藏列表（分页）
 *   GET    /api/favorite/check/{productId}   - 查询是否已收藏
 *   DELETE /api/favorite/{productId}         - 取消收藏
 *
 * ⚠️ 注意: 需要在 WebMvcConfig.java 的白名单中确保 /favorite/** 路径受 JWT 拦截
 */
@Api(tags = "商品收藏")
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    // =====================================================================
    // 接口1: 收藏/取消收藏（幂等切换）
    // POST /api/favorite/toggle/{productId}
    // =====================================================================

    /**
     * 收藏 / 取消收藏（幂等切换）
     *
     * 📌 逻辑: 已收藏则取消，未收藏则添加
     * 📌 前端: 点击心形按钮调用此接口，根据返回值切换红/灰心
     *
     * @param productId 商品ID
     * @return { isFavorite: true/false }
     *         true  = 当前已收藏（刚刚收藏）
     *         false = 当前已取消（刚刚取消）
     */
    @ApiOperation("收藏/取消收藏")
    @PostMapping("/toggle/{productId}")
    public Result<Map<String, Object>> toggleFavorite(
            @ApiParam("商品ID") @PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        boolean isFavorite = favoriteService.toggleFavorite(userId, productId);

        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", isFavorite);
        data.put("message", isFavorite ? "收藏成功" : "已取消收藏");
        return Result.success(data);
    }

    // =====================================================================
    // 接口2: 我的收藏列表（分页）
    // GET /api/favorite/list?pageNum=1&pageSize=12
    // =====================================================================

    /**
     * 获取我的收藏列表
     *
     * 📌 返回字段:
     *   - productId     商品ID
     *   - productImage  商品图片
     *   - productName   商品名称
     *   - currentPrice  当前价格（下架则为 null）
     *   - favoritePrice 收藏时价格
     *   - isOffShelf    是否下架
     *   - isPriceDown   是否降价
     *   - createTime    收藏时间
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认12）
     * @return 分页收藏列表
     */
    @ApiOperation("我的收藏列表")
    @GetMapping("/list")
    public Result<Page<FavoriteVO>> getFavoriteList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "12") Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<FavoriteVO> page = favoriteService.getFavoriteList(userId, pageNum, pageSize);
        return Result.success(page);
    }

    // =====================================================================
    // 接口3: 查询是否已收藏（商品详情页用）
    // GET /api/favorite/check/{productId}
    // =====================================================================

    /**
     * 查询是否已收藏某商品
     *
     * 📌 场景: 进入商品详情页时调用，同步收藏按钮状态
     * 📌 前端: 根据 isFavorite 决定按钮颜色（红心/灰心）
     *
     * @param productId 商品ID
     * @return { isFavorite: true/false }
     */
    @ApiOperation("查询是否已收藏")
    @GetMapping("/check/{productId}")
    public Result<Map<String, Object>> checkFavorite(
            @ApiParam("商品ID") @PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        boolean isFavorite = favoriteService.checkFavorite(userId, productId);

        Map<String, Object> data = new HashMap<>();
        data.put("isFavorite", isFavorite);
        return Result.success(data);
    }

    // =====================================================================
    // 接口4: 取消收藏
    // DELETE /api/favorite/{productId}
    // =====================================================================

    /**
     * 取消收藏
     *
     * 📌 场景: 收藏列表页，点击"取消收藏"按钮
     * 📌 注意: 与 toggle 的区别是这个接口语义更明确，不会误收藏
     *
     * @param productId 商品ID
     * @return 成功消息
     */
    @ApiOperation("取消收藏")
    @DeleteMapping("/{productId}")
    public Result<Void> removeFavorite(
            @ApiParam("商品ID") @PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        favoriteService.removeFavorite(userId, productId);
        return Result.success();
    }
}
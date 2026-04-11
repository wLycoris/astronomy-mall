package com.astronomy.mall.module.recommend.service;

import com.astronomy.mall.module.recommend.dto.BrowseLogDTO;
import com.astronomy.mall.module.recommend.dto.PostBrowseLogDTO;
import com.astronomy.mall.module.recommend.dto.RecommendClickDTO;
import com.astronomy.mall.module.recommend.vo.RecommendPostVO;
import com.astronomy.mall.module.recommend.vo.RecommendProductVO;

import java.util.List;

/**
 * 推荐系统核心 Service
 *
 * 职责：
 * 1. 浏览埋点（商品/帖子）
 * 2. 商品推荐（首页/相似/购物车）
 * 3. 跨模块联动推荐（识别→课程 / 完课→下一门 / 签到→商品 / 帖子推荐）
 * 4. 推荐点击回写
 *
 * 算法说明：
 * - 内容相似度: 标签 Jaccard + 分类 + 价格区间加权
 * - 协同过滤: Item-based CF（委托给 CfRecommendService）
 * - 冷启动: interest_tags → 热门排序 → 随机抽样
 * - 融合公式: content×0.6 + cf×0.3 + coldstart×0.1
 */
public interface RecommendService {

    // ======================== 浏览埋点 ========================

    /**
     * 记录商品浏览（Redis 30分钟去重）
     */
    void logProductBrowse(Long userId, BrowseLogDTO dto);

    /**
     * 记录帖子浏览（Redis 30分钟去重）
     */
    void logPostBrowse(Long userId, PostBrowseLogDTO dto);

    // ======================== 商品推荐 ========================

    /**
     * 首页个性化推荐（混合策略）
     * 未登录 → 热门商品；已登录 → content×0.6 + cf×0.3 + coldstart×0.1
     */
    List<RecommendProductVO> getHomeRecommend(Long userId, int limit);

    /**
     * 相似商品推荐（商品详情页下方）
     * 基于当前商品的 tags + categoryId + price 计算内容相似度
     */
    List<RecommendProductVO> getSimilarProducts(Long productId, int limit);

    /**
     * 购物车推荐（购物车页面底部）
     * 根据购物车内商品的 tags 并集推荐互补商品
     */
    List<RecommendProductVO> getCartRecommend(Long userId, int limit);

    // ======================== 跨模块联动 ========================

    /**
     * AI识别结果 → 推荐相关课程
     * 基于 machine_tags 匹配课程 tags
     */
    List<Object> getRecognitionCourseRecommend(Long recognitionId, int limit);

    /**
     * 完课 → 推荐下一门课程
     * 基于已学课程 tags 匹配相似课程
     */
    List<Object> getNextCourseRecommend(Long userId, Long courseId, int limit);

    /**
     * 签到观测点 → 推荐适合器材
     * 基于海拔/光污染等级前置过滤 + tags Jaccard 排序
     */
    List<RecommendProductVO> getSpotEquipmentRecommend(Long spotId, int limit);

    /**
     * 帖子个性化推荐（论坛"为你推荐"Tab）
     * 基于浏览历史的帖子 tags 内容相似度
     */
    List<RecommendPostVO> getPostRecommend(Long userId, int limit);

    // ======================== 推荐效果 ========================

    /**
     * 记录推荐点击（前端点击推荐卡片时回写）
     */
    void recordClick(Long userId, RecommendClickDTO dto);

    // ======================== 算法函数（内部调用） ========================

    /**
     * 标签 Jaccard 相似度
     * |A ∩ B| / |A ∪ B|，两个标签集合都为空时返回 0
     */
    double jaccardSimilarity(String tags1Json, String tags2Json);

    /**
     * 价格区间相似度（归一化，越近越相似）
     */
    double priceSimilarity(java.math.BigDecimal p1, java.math.BigDecimal p2);

    /**
     * 商品综合内容相似度（标签×0.5 + 类别×0.3 + 价格×0.2）
     */
    double contentSimilarity(com.astronomy.mall.module.product.entity.Product p1,
                             com.astronomy.mall.module.product.entity.Product p2);

    /**
     * 帖子内容相似度（纯标签 Jaccard）
     */
    double postContentSimilarity(String tags1Json, String tags2Json);
}

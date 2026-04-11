package com.astronomy.mall.module.recommend.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.module.product.entity.Product;
import com.astronomy.mall.module.product.mapper.ProductMapper;
import com.astronomy.mall.module.recommend.dto.BrowseLogDTO;
import com.astronomy.mall.module.recommend.dto.PostBrowseLogDTO;
import com.astronomy.mall.module.recommend.dto.RecommendClickDTO;
import com.astronomy.mall.module.recommend.entity.BrowseLog;
import com.astronomy.mall.module.recommend.entity.PostBrowseLog;
import com.astronomy.mall.module.recommend.entity.RecommendRecord;
import com.astronomy.mall.module.recommend.mapper.BrowseLogMapper;
import com.astronomy.mall.module.recommend.mapper.PostBrowseLogMapper;
import com.astronomy.mall.module.recommend.mapper.RecommendRecordMapper;
import com.astronomy.mall.module.cart.mapper.CartMapper;
import com.astronomy.mall.module.cart.vo.CartVO;
import com.astronomy.mall.module.forum.entity.Post;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.recommend.service.CfRecommendService;
import com.astronomy.mall.module.recommend.service.RecommendService;
import com.astronomy.mall.module.recommend.vo.RecommendPostVO;
import com.astronomy.mall.module.recommend.vo.RecommendProductVO;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 推荐系统核心实现
 *
 * 📌 算法说明（论文第 4/5 章对应）:
 * 本系统采用「标签 Jaccard 相似度 + 多维特征加权求和」实现内容推荐，
 * 借鉴向量化思想将商品/帖子特征映射为多维向量后计算相似度，
 * 无需加载外部模型，纯 Java 实现。
 *
 * 📌 混合策略权重（可在 application.yml 热调）:
 * - 内容相似度(content-weight): 0.6
 * - 协同过滤(cf-weight): 0.3
 * - 冷启动(coldstart-weight): 0.1
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    @Resource
    private BrowseLogMapper browseLogMapper;
    @Resource
    private PostBrowseLogMapper postBrowseLogMapper;
    @Resource
    private RecommendRecordMapper recommendRecordMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CfRecommendService cfRecommendService;
    @Resource
    private CartMapper cartMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 内容相似度权重 */
    @Value("${recommend.content-weight:0.6}")
    private double contentWeight;

    /** 协同过滤权重 */
    @Value("${recommend.cf-weight:0.3}")
    private double cfWeight;

    /** 冷启动权重 */
    @Value("${recommend.coldstart-weight:0.1}")
    private double coldstartWeight;

    /** 帖子推荐不足时是否用 hot_score 兜底 */
    @Value("${recommend.post-hot-fallback:true}")
    private boolean postHotFallback;

    // ======================== 浏览埋点 ========================

    @Override
    public void logProductBrowse(Long userId, BrowseLogDTO dto) {
        // Redis 30分钟去重: 同一用户同一商品 30 分钟内只记录一次
        String dedupKey = "browse:dedup:" + userId + ":" + dto.getProductId();
        Boolean isNew = stringRedisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", 30, TimeUnit.MINUTES);

        if (Boolean.TRUE.equals(isNew)) {
            BrowseLog browseLog = new BrowseLog();
            browseLog.setUserId(userId);
            browseLog.setProductId(dto.getProductId());
            browseLog.setCategoryId(dto.getCategoryId());
            browseLog.setSource(dto.getSource() != null ? dto.getSource() : "detail");
            browseLog.setBrowseTime(LocalDateTime.now());
            browseLogMapper.insert(browseLog);
            log.debug("商品浏览埋点: userId={}, productId={}", userId, dto.getProductId());
        }
    }

    @Override
    public void logPostBrowse(Long userId, PostBrowseLogDTO dto) {
        // Redis 30分钟去重
        String dedupKey = "post:browse:dedup:" + userId + ":" + dto.getPostId();
        Boolean isNew = stringRedisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", 30, TimeUnit.MINUTES);

        if (Boolean.TRUE.equals(isNew)) {
            PostBrowseLog browseLog = new PostBrowseLog();
            browseLog.setUserId(userId);
            browseLog.setPostId(dto.getPostId());
            browseLog.setBrowseTime(LocalDateTime.now());
            browseLog.setDuration(dto.getDuration() != null ? dto.getDuration() : 0);
            browseLog.setSource(dto.getSource());
            postBrowseLogMapper.insert(browseLog);
            log.debug("帖子浏览埋点: userId={}, postId={}", userId, dto.getPostId());
        }
    }

    // ======================== 商品推荐 ========================

    /**
     * 首页个性化推荐（混合策略）
     *
     * 📌 融合公式:
     * finalScore = contentSim × 0.6 + cfSim × 0.3 + coldStartBoost × 0.1
     *
     * 📌 流程:
     * 1. 未登录 → 直接走冷启动（热门商品）
     * 2. 已登录 →
     *    a. 获取用户近 30 条浏览商品的 tags 并集
     *    b. 获取 CF 推荐候选集
     *    c. 获取冷启动候选集（interest_tags 匹配）
     *    d. 对所有候选商品计算融合得分，排序取 Top N
     *    e. 异步记录曝光
     */
    @Override
    public List<RecommendProductVO> getHomeRecommend(Long userId, int limit) {
        // 未登录: 冷启动兜底
        if (userId == null) {
            List<RecommendProductVO> result = coldStart(null, limit);
            saveExposureRecords(null, "product", "coldstart", result);
            return result;
        }

        // 1. 获取用户近期浏览的商品列表
        List<Long> recentProductIds = browseLogMapper.selectRecentProductIds(userId, 30);

        // 无浏览记录: 走冷启动
        if (recentProductIds.isEmpty()) {
            List<RecommendProductVO> result = coldStart(userId, limit);
            saveExposureRecords(userId, "product", "coldstart", result);
            return result;
        }

        // 2. 加载近期浏览商品实体（用于计算内容相似度）
        List<Product> recentProducts = productMapper.selectBatchIds(recentProductIds);
        // 合并用户浏览商品的 tags 为一个并集（代表用户近期兴趣画像）
        Set<String> userTagProfile = new HashSet<>();
        for (Product p : recentProducts) {
            userTagProfile.addAll(parseTags(p.getTags()));
        }

        // 3. 获取候选商品集合（排除已浏览的）
        Set<Long> excludeIds = new HashSet<>(recentProductIds);
        // 扩大候选池: 取上架商品（最多200个，避免全表扫描）
        LambdaQueryWrapper<Product> candidateWrapper = new LambdaQueryWrapper<>();
        candidateWrapper.eq(Product::getStatus, 1)
                .notIn(Product::getId, excludeIds)
                .last("LIMIT 200");
        List<Product> candidates = productMapper.selectList(candidateWrapper);

        if (candidates.isEmpty()) {
            List<RecommendProductVO> result = coldStart(userId, limit);
            saveExposureRecords(userId, "product", "coldstart", result);
            return result;
        }

        // 4. 计算内容相似度得分: 候选商品 tags 与用户画像 tags 的 Jaccard
        String userTagsJson = JSON.toJSONString(new ArrayList<>(userTagProfile));
        Map<Long, Double> contentScores = new HashMap<>();
        for (Product candidate : candidates) {
            double sim = jaccardSimilarity(userTagsJson, candidate.getTags());
            if (sim > 0) {
                contentScores.put(candidate.getId(), sim);
            }
        }

        // 5. 获取 CF 推荐得分
        Map<Long, Double> cfScores = cfRecommendService.getCfRecommendForUser(userId, 50);

        // 6. 冷启动加分: interest_tags 匹配的候选商品额外 +0.1
        User user = userMapper.selectById(userId);
        Set<String> interestTags = (user != null && user.getInterestTags() != null)
                ? parseTags(user.getInterestTags()) : Collections.emptySet();

        // 7. 融合得分计算
        Map<Long, Double> finalScores = new HashMap<>();
        Map<Long, String> algorithmMap = new HashMap<>();
        for (Product candidate : candidates) {
            Long pid = candidate.getId();
            double contentSim = contentScores.getOrDefault(pid, 0.0);
            double cfSim = cfScores.getOrDefault(pid, 0.0);

            // 冷启动加分: 候选商品 tags 与用户 interest_tags 有交集
            double coldStartBoost = 0.0;
            if (!interestTags.isEmpty()) {
                Set<String> candidateTags = parseTags(candidate.getTags());
                long hitCount = candidateTags.stream().filter(interestTags::contains).count();
                if (hitCount > 0) {
                    coldStartBoost = (double) hitCount / Math.max(interestTags.size(), candidateTags.size());
                }
            }

            double finalScore = contentSim * contentWeight + cfSim * cfWeight + coldStartBoost * coldstartWeight;

            if (finalScore > 0) {
                finalScores.put(pid, finalScore);
                // 标记主要算法来源
                if (contentSim >= cfSim && contentSim >= coldStartBoost) {
                    algorithmMap.put(pid, "content");
                } else if (cfSim >= contentSim) {
                    algorithmMap.put(pid, "collaborative");
                } else {
                    algorithmMap.put(pid, "coldstart");
                }
            }
        }

        // 8. 排序取 Top N
        Map<Long, Product> candidateMap = candidates.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<RecommendProductVO> result = finalScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Product p = candidateMap.get(entry.getKey());
                    String algo = algorithmMap.getOrDefault(entry.getKey(), "content");
                    String reason;
                    switch (algo) {
                        case "collaborative": reason = "购买相似商品的用户也在看"; break;
                        case "coldstart": reason = "根据你的兴趣推荐"; break;
                        default: reason = "根据你的浏览推荐"; break;
                    }
                    RecommendProductVO vo = toProductVO(p, reason, algo);
                    vo.setScore(entry.getValue());
                    return vo;
                })
                .collect(Collectors.toList());

        // 不足 limit 时用冷启动补齐
        if (result.size() < limit) {
            List<RecommendProductVO> fallback = coldStart(userId, limit - result.size());
            Set<Long> existIds = result.stream().map(RecommendProductVO::getId).collect(Collectors.toSet());
            for (RecommendProductVO vo : fallback) {
                if (!existIds.contains(vo.getId())) {
                    result.add(vo);
                }
            }
        }

        // 9. 异步记录曝光
        saveExposureRecords(userId, "product", "mixed", result);
        return result;
    }

    /**
     * 相似商品推荐（商品详情页下方）
     *
     * 📌 算法:
     * 1. 加载目标商品
     * 2. 取同类别 + 同标签商品作为候选集
     * 3. 按 contentSimilarity（标签×0.5 + 类别×0.3 + 价格×0.2）排序
     * 4. 异步记录曝光
     */
    @Override
    public List<RecommendProductVO> getSimilarProducts(Long productId, int limit) {
        Product target = productMapper.selectById(productId);
        if (target == null) {
            return Collections.emptyList();
        }

        // 候选集: 同类别或有共同标签的上架商品（排除自身，最多100个）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .ne(Product::getId, productId)
                .and(w -> {
                    // 同类别
                    w.eq(Product::getCategoryId, target.getCategoryId());
                    // 或有共同标签
                    Set<String> targetTags = parseTags(target.getTags());
                    for (String tag : targetTags) {
                        w.or().like(Product::getTags, "\"" + tag + "\"");
                    }
                })
                .last("LIMIT 100");
        List<Product> candidates = productMapper.selectList(wrapper);

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // 按内容相似度排序
        List<RecommendProductVO> result = candidates.stream()
                .map(candidate -> {
                    double sim = contentSimilarity(target, candidate);
                    RecommendProductVO vo = toProductVO(candidate, "相似商品", "content");
                    vo.setScore(sim);
                    return vo;
                })
                .filter(vo -> vo.getScore() > 0)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());

        // 异步记录曝光
        saveExposureRecords(null, "product", "content", result);
        return result;
    }

    /**
     * 购物车推荐（购物车页面底部）
     *
     * 📌 算法:
     * 1. 获取购物车内所有商品的 tags 并集
     * 2. 取匹配标签的上架商品作为候选集（排除购物车内的商品）
     * 3. 按 Jaccard 相似度排序
     * 4. 异步记录曝光
     */
    @Override
    public List<RecommendProductVO> getCartRecommend(Long userId, int limit) {
        // 1. 获取购物车商品
        List<CartVO> cartItems = cartMapper.selectCartListWithProduct(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            return coldStart(userId, limit);
        }

        // 2. 收集购物车商品ID和tags并集
        Set<Long> cartProductIds = new HashSet<>();
        Set<String> cartTagsUnion = new HashSet<>();
        for (CartVO cartItem : cartItems) {
            cartProductIds.add(cartItem.getProductId());
            // 从数据库查商品获取 tags
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product != null) {
                cartTagsUnion.addAll(parseTags(product.getTags()));
            }
        }

        if (cartTagsUnion.isEmpty()) {
            return coldStart(userId, limit);
        }

        // 3. 查找匹配标签的候选商品（排除购物车内已有的）
        String cartTagsJson = JSON.toJSONString(new ArrayList<>(cartTagsUnion));
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .notIn(Product::getId, cartProductIds)
                .and(w -> {
                    boolean first = true;
                    for (String tag : cartTagsUnion) {
                        if (first) {
                            w.like(Product::getTags, "\"" + tag + "\"");
                            first = false;
                        } else {
                            w.or().like(Product::getTags, "\"" + tag + "\"");
                        }
                    }
                })
                .last("LIMIT 100");
        List<Product> candidates = productMapper.selectList(wrapper);

        if (candidates.isEmpty()) {
            return coldStart(userId, limit);
        }

        // 4. 按 Jaccard 相似度排序
        List<RecommendProductVO> result = candidates.stream()
                .map(candidate -> {
                    double sim = jaccardSimilarity(cartTagsJson, candidate.getTags());
                    RecommendProductVO vo = toProductVO(candidate, "购物车关联推荐", "content");
                    vo.setScore(sim);
                    return vo;
                })
                .filter(vo -> vo.getScore() > 0)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());

        saveExposureRecords(userId, "product", "content", result);
        return result;
    }

    // ======================== 跨模块联动 ========================

    @Override
    public List<Object> getRecognitionCourseRecommend(Long recognitionId, int limit) {
        // TODO 8.3 填充: machine_tags → 课程 tags 匹配
        return Collections.emptyList();
    }

    @Override
    public List<Object> getNextCourseRecommend(Long userId, Long courseId, int limit) {
        // TODO 8.3 填充: 基于已学课程 tags 推荐下一门
        return Collections.emptyList();
    }

    @Override
    public List<RecommendProductVO> getSpotEquipmentRecommend(Long spotId, int limit) {
        // TODO 8.3 填充: 海拔/光污染 → 器材过滤 + tags 排序
        return Collections.emptyList();
    }

    /**
     * 帖子个性化推荐（论坛"为你推荐"Tab）
     *
     * 📌 算法:
     * 1. 已登录 → 获取用户近期浏览帖子的 tags 并集作为兴趣画像
     * 2. 候选集: 已发布(status=2)帖子中与画像 tags 有交集的
     * 3. 按 postContentSimilarity（纯标签 Jaccard）排序
     * 4. 不足时用 hot_score 兜底
     * 5. 未登录 → 直接返回热门帖子
     */
    @Override
    public List<RecommendPostVO> getPostRecommend(Long userId, int limit) {
        // 未登录或无浏览历史: 热门帖子兜底
        if (userId == null) {
            return getHotPosts(limit);
        }

        // 1. 获取用户近期浏览的帖子
        List<Long> recentPostIds = postBrowseLogMapper.selectRecentPostIds(userId, 30);
        if (recentPostIds.isEmpty()) {
            // 无浏览历史: 尝试用 interest_tags 匹配帖子
            User user = userMapper.selectById(userId);
            if (user != null && user.getInterestTags() != null && !"[]".equals(user.getInterestTags())) {
                return getPostsByUserInterest(user.getInterestTags(), userId, limit);
            }
            return getHotPosts(limit);
        }

        // 2. 合并浏览帖子的 tags 为用户画像
        List<Post> recentPosts = postMapper.selectBatchIds(recentPostIds);
        Set<String> userTagProfile = new HashSet<>();
        for (Post p : recentPosts) {
            userTagProfile.addAll(parseTags(p.getTags()));
        }

        if (userTagProfile.isEmpty()) {
            return getHotPosts(limit);
        }

        // 3. 候选帖子: 已发布 + 有标签交集 + 排除已浏览
        String userTagsJson = JSON.toJSONString(new ArrayList<>(userTagProfile));
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 2)
                .notIn(Post::getId, recentPostIds)
                .and(w -> {
                    boolean first = true;
                    for (String tag : userTagProfile) {
                        if (first) {
                            w.like(Post::getTags, "\"" + tag + "\"");
                            first = false;
                        } else {
                            w.or().like(Post::getTags, "\"" + tag + "\"");
                        }
                    }
                })
                .last("LIMIT 100");
        List<Post> candidates = postMapper.selectList(wrapper);

        // 4. 按帖子内容相似度排序
        List<RecommendPostVO> result = candidates.stream()
                .map(post -> {
                    double sim = postContentSimilarity(userTagsJson, post.getTags());
                    RecommendPostVO vo = toPostVO(post, "content");
                    vo.setScore(sim);
                    return vo;
                })
                .filter(vo -> vo.getScore() > 0)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());

        // 5. 不足 limit 时用 hot_score 兜底
        if (postHotFallback && result.size() < limit) {
            Set<Long> existIds = result.stream().map(RecommendPostVO::getId).collect(Collectors.toSet());
            existIds.addAll(recentPostIds);
            List<RecommendPostVO> hotFallback = getHotPosts(limit - result.size()).stream()
                    .filter(vo -> !existIds.contains(vo.getId()))
                    .collect(Collectors.toList());
            result.addAll(hotFallback);
        }

        saveExposureRecords(userId, "post", "content", result);
        return result;
    }

    /**
     * 根据用户 interest_tags 匹配帖子（冷启动分支）
     */
    private List<RecommendPostVO> getPostsByUserInterest(String interestTagsJson, Long userId, int limit) {
        Set<String> tags = parseTags(interestTagsJson);
        if (tags.isEmpty()) {
            return getHotPosts(limit);
        }

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 2)
                .ne(Post::getUserId, userId)
                .and(w -> {
                    boolean first = true;
                    for (String tag : tags) {
                        if (first) {
                            w.like(Post::getTags, "\"" + tag + "\"");
                            first = false;
                        } else {
                            w.or().like(Post::getTags, "\"" + tag + "\"");
                        }
                    }
                })
                .orderByDesc(Post::getHotScore)
                .last("LIMIT " + limit);
        List<Post> posts = postMapper.selectList(wrapper);
        return posts.stream()
                .map(p -> toPostVO(p, "coldstart"))
                .collect(Collectors.toList());
    }

    /**
     * 热门帖子（按 hot_score 倒序）
     */
    private List<RecommendPostVO> getHotPosts(int limit) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 2)
                .orderByDesc(Post::getHotScore)
                .last("LIMIT " + limit);
        List<Post> posts = postMapper.selectList(wrapper);
        return posts.stream()
                .map(p -> toPostVO(p, "hot"))
                .collect(Collectors.toList());
    }

    /**
     * Post → RecommendPostVO 转换
     */
    private RecommendPostVO toPostVO(Post post, String algorithm) {
        RecommendPostVO vo = new RecommendPostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setTags(post.getTags());
        vo.setHotScore(post.getHotScore());
        vo.setLikeCount(post.getLikeCount());
        vo.setAlgorithm(algorithm);

        // 封面图: images JSON 数组的第一张
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            try {
                List<String> images = JSON.parseArray(post.getImages(), String.class);
                if (!images.isEmpty()) {
                    vo.setCoverImage(images.get(0));
                }
            } catch (Exception e) {
                // 解析失败忽略
            }
        }

        // 作者信息需要额外查询（帖子实体不含作者昵称）
        if (post.getUserId() != null) {
            User author = userMapper.selectById(post.getUserId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
        }

        return vo;
    }

    // ======================== 推荐效果 ========================

    @Override
    public void recordClick(Long userId, RecommendClickDTO dto) {
        // 找到最近一条匹配的未点击曝光记录，回写 is_clicked + click_time
        LambdaUpdateWrapper<RecommendRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RecommendRecord::getUserId, userId)
                .eq(RecommendRecord::getRecommendType, dto.getRecommendType())
                .eq(RecommendRecord::getTargetId, dto.getTargetId())
                .eq(RecommendRecord::getIsClicked, 0)
                .set(RecommendRecord::getIsClicked, 1)
                .set(RecommendRecord::getClickTime, LocalDateTime.now())
                .last("LIMIT 1");
        recommendRecordMapper.update(null, wrapper);
        log.debug("推荐点击回写: userId={}, type={}, targetId={}",
                userId, dto.getRecommendType(), dto.getTargetId());
    }

    // ======================== 算法函数 ========================

    /**
     * 标签 Jaccard 相似度
     * 公式: |A ∩ B| / |A ∪ B|
     * 两个标签集合都为空时返回 0（无标签对不算相似）
     */
    @Override
    public double jaccardSimilarity(String tags1Json, String tags2Json) {
        Set<String> set1 = parseTags(tags1Json);
        Set<String> set2 = parseTags(tags2Json);
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0;
        }
        long intersection = set1.stream().filter(set2::contains).count();
        long union = Stream.concat(set1.stream(), set2.stream()).distinct().count();
        return union == 0 ? 0 : (double) intersection / union;
    }

    /**
     * 价格区间相似度（归一化，越近越相似）
     * 公式: max(0, 1 - |p1-p2| / maxPrice)
     * maxPrice = 20000（天文器材价格上限估值）
     */
    @Override
    public double priceSimilarity(BigDecimal p1, BigDecimal p2) {
        if (p1 == null || p2 == null) {
            return 0;
        }
        double maxPrice = 20000.0;
        double diff = Math.abs(p1.doubleValue() - p2.doubleValue());
        return Math.max(0, 1 - diff / maxPrice);
    }

    /**
     * 商品综合内容相似度（加权求和）
     * 权重: 标签 0.5 + 类别 0.3 + 价格 0.2
     *
     * 📌 论文可描述为「基于特征向量加权相似度的内容推荐算法」
     */
    @Override
    public double contentSimilarity(Product p1, Product p2) {
        double tagSim = jaccardSimilarity(p1.getTags(), p2.getTags());
        double categorySim = (p1.getCategoryId() != null && p1.getCategoryId().equals(p2.getCategoryId()))
                ? 1.0 : 0.0;
        double priceSim = priceSimilarity(p1.getPrice(), p2.getPrice());
        // 加权求和
        return tagSim * 0.5 + categorySim * 0.3 + priceSim * 0.2;
    }

    /**
     * 帖子内容相似度（纯标签 Jaccard）
     * 帖子不需要价格/类别维度，只看标签集合相似
     */
    @Override
    public double postContentSimilarity(String tags1Json, String tags2Json) {
        return jaccardSimilarity(tags1Json, tags2Json);
    }

    // ======================== 冷启动三级兜底 ========================

    /**
     * 冷启动推荐（三级兜底策略）
     * Level 1: 用户有 interest_tags → 匹配商品 tags，按 sales 倒序
     * Level 2: 按 (sales × 0.6 + is_recommend × 0.4) 综合评分排序
     * Level 3: 随机抽样（极端兜底）
     */
    private List<RecommendProductVO> coldStart(Long userId, int limit) {
        // Level 1: 基于兴趣标签匹配
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getInterestTags() != null
                    && !user.getInterestTags().isEmpty()
                    && !"[]".equals(user.getInterestTags())) {
                List<String> tags = parseTags(user.getInterestTags()).stream()
                        .collect(Collectors.toList());
                List<Product> matched = selectByTagsAny(tags, limit);
                if (!matched.isEmpty()) {
                    return matched.stream()
                            .map(p -> toProductVO(p, "兴趣标签匹配", "coldstart"))
                            .collect(Collectors.toList());
                }
            }
        }

        // Level 2: 热门商品排序
        List<Product> hotProducts = selectHotProducts(limit);
        if (!hotProducts.isEmpty()) {
            return hotProducts.stream()
                    .map(p -> toProductVO(p, "热门推荐", "hot"))
                    .collect(Collectors.toList());
        }

        // Level 3: 随机抽样
        List<Product> randomProducts = selectRandom(limit);
        return randomProducts.stream()
                .map(p -> toProductVO(p, "随机推荐", "coldstart"))
                .collect(Collectors.toList());
    }

    // ======================== 内部工具方法 ========================

    /**
     * 解析 JSON 数组格式的标签字符串为 Set
     */
    private Set<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.trim().isEmpty() || "[]".equals(tagsJson.trim())) {
            return Collections.emptySet();
        }
        try {
            List<String> list = JSON.parseArray(tagsJson, String.class);
            return new HashSet<>(list);
        } catch (Exception e) {
            log.warn("标签JSON解析失败: {}", tagsJson);
            return Collections.emptySet();
        }
    }

    /**
     * Product → RecommendProductVO 转换
     */
    private RecommendProductVO toProductVO(Product p, String reason, String algorithm) {
        RecommendProductVO vo = new RecommendProductVO();
        vo.setId(p.getId());
        vo.setProductName(p.getProductName());
        vo.setMainImage(p.getMainImage());
        vo.setPrice(p.getPrice());
        vo.setOriginalPrice(p.getOriginalPrice());
        vo.setSales(p.getSales());
        vo.setReason(reason);
        vo.setAlgorithm(algorithm);
        return vo;
    }

    /**
     * 异步记录推荐曝光（不阻塞主流程）
     */
    @Async("notificationExecutor")
    public void saveExposureRecords(Long userId, String recommendType, String algorithm,
                                     List<? extends Object> items) {
        if (userId == null || items == null || items.isEmpty()) {
            return;
        }
        try {
            for (int i = 0; i < items.size(); i++) {
                RecommendRecord record = new RecommendRecord();
                record.setUserId(userId);
                record.setRecommendType(recommendType);
                record.setAlgorithm(algorithm);
                record.setPosition(i + 1);
                record.setIsClicked(0);

                if (items.get(i) instanceof RecommendProductVO) {
                    RecommendProductVO vo = (RecommendProductVO) items.get(i);
                    record.setTargetId(vo.getId());
                    record.setScore(vo.getScore());
                    record.setAlgorithm(vo.getAlgorithm() != null ? vo.getAlgorithm() : algorithm);
                } else if (items.get(i) instanceof RecommendPostVO) {
                    RecommendPostVO vo = (RecommendPostVO) items.get(i);
                    record.setTargetId(vo.getId());
                    record.setScore(vo.getScore());
                    record.setAlgorithm(vo.getAlgorithm() != null ? vo.getAlgorithm() : algorithm);
                }

                recommendRecordMapper.insert(record);
            }
        } catch (Exception e) {
            log.error("推荐曝光记录保存失败", e);
        }
    }

    // ======================== 数据库查询（封装 ProductMapper 调用） ========================

    /**
     * 根据标签列表匹配商品（任一标签命中即可）
     * SQL: tags LIKE '%"标签1"%' OR tags LIKE '%"标签2"%' ...
     */
    private List<Product> selectByTagsAny(List<String> tags, int limit) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        // 用 OR 拼接每个标签的 LIKE 条件
        wrapper.and(w -> {
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                if (i == 0) {
                    w.like(Product::getTags, "\"" + tag + "\"");
                } else {
                    w.or().like(Product::getTags, "\"" + tag + "\"");
                }
            }
        });
        wrapper.orderByDesc(Product::getSales);
        wrapper.last("LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }

    /**
     * 热门商品排序: 按 sales 倒序 + is_recommend 优先
     */
    private List<Product> selectHotProducts(int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .orderByDesc(Product::getIsRecommend)
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }

    /**
     * 随机抽取商品（极端兜底）
     */
    private List<Product> selectRandom(int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
                .last("ORDER BY RAND() LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }
}

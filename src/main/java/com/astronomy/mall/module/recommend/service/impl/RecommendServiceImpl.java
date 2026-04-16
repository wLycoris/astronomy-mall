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
import com.astronomy.mall.module.course.entity.Course;
import com.astronomy.mall.module.course.entity.CourseProgress;
import com.astronomy.mall.module.course.mapper.CourseMapper;
import com.astronomy.mall.module.course.mapper.CourseProgressMapper;
import com.astronomy.mall.module.course.vo.CourseVO;
import com.astronomy.mall.module.forum.entity.Post;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.location.entity.ObservationSpot;
import com.astronomy.mall.module.location.mapper.ObservationSpotMapper;
import com.astronomy.mall.module.recognition.entity.Recognition;
import com.astronomy.mall.module.recognition.mapper.RecognitionMapper;
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

import com.alibaba.fastjson.TypeReference;

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

    // 8.3 跨模块联动所需
    @Resource
    private RecognitionMapper recognitionMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CourseProgressMapper courseProgressMapper;
    @Resource
    private ObservationSpotMapper observationSpotMapper;

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
        // 🆕 8.2 优雅降级: Redis 不可用时跳过去重直接写库，不影响埋点
        Boolean isNew = null;
        try {
            String dedupKey = "browse:dedup:" + userId + ":" + dto.getProductId();
            isNew = stringRedisTemplate.opsForValue()
                    .setIfAbsent(dedupKey, "1", 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 浏览去重失败, 降级直接写库: {}", e.getMessage());
        }

        // Redis 正常且非新浏览则跳过；Redis 异常(isNew==null)时直接写库
        if (isNew == null || Boolean.TRUE.equals(isNew)) {
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
        // 🆕 8.2 优雅降级: Redis 不可用时跳过去重直接写库
        Boolean isNew = null;
        try {
            String dedupKey = "post:browse:dedup:" + userId + ":" + dto.getPostId();
            isNew = stringRedisTemplate.opsForValue()
                    .setIfAbsent(dedupKey, "1", 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 帖子浏览去重失败, 降级直接写库: {}", e.getMessage());
        }

        // Redis 正常且非新浏览则跳过；Redis 异常(isNew==null)时直接写库
        if (isNew == null || Boolean.TRUE.equals(isNew)) {
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
        // 未登录: 冷启动兜底（不缓存，每次实时取热门）
        if (userId == null) {
            List<RecommendProductVO> result = coldStart(null, limit);
            saveExposureRecords(null, "product", "coldstart", result);
            return result;
        }

        // 📌 8.2 Redis 缓存: recommend:product:home:{userId} TTL=30min
        String cacheKey = "recommend:product:home:" + userId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                List<RecommendProductVO> cacheResult = JSON.parseObject(cached,
                        new TypeReference<List<RecommendProductVO>>() {});
                if (cacheResult != null && !cacheResult.isEmpty()) {
                    log.debug("首页推荐命中缓存: userId={}, size={}", userId, cacheResult.size());
                    return cacheResult;
                }
            }
        } catch (Exception e) {
            log.warn("Redis 读取首页推荐缓存失败, 降级实时计算: {}", e.getMessage());
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

        // 9. 写入 Redis 缓存（30分钟过期）
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入首页推荐缓存失败: {}", e.getMessage());
        }

        // 10. 异步记录曝光
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
        // 📌 8.2 Redis 缓存: recommend:product:similar:{productId} TTL=1h
        String cacheKey = "recommend:product:similar:" + productId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                List<RecommendProductVO> cacheResult = JSON.parseObject(cached,
                        new TypeReference<List<RecommendProductVO>>() {});
                if (cacheResult != null && !cacheResult.isEmpty()) {
                    log.debug("相似商品命中缓存: productId={}, size={}", productId, cacheResult.size());
                    return cacheResult;
                }
            }
        } catch (Exception e) {
            log.warn("Redis 读取相似商品缓存失败: {}", e.getMessage());
        }

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

        // 写入 Redis 缓存（1小时过期，相似商品变化频率低）
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 写入相似商品缓存失败: {}", e.getMessage());
        }

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
        // 📌 8.2 Redis 缓存: recommend:product:cart:{userId} TTL=10min（购物车变化频繁，短缓存）
        String cacheKey = "recommend:product:cart:" + userId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                List<RecommendProductVO> cacheResult = JSON.parseObject(cached,
                        new TypeReference<List<RecommendProductVO>>() {});
                if (cacheResult != null && !cacheResult.isEmpty()) {
                    log.debug("购物车推荐命中缓存: userId={}, size={}", userId, cacheResult.size());
                    return cacheResult;
                }
            }
        } catch (Exception e) {
            log.warn("Redis 读取购物车推荐缓存失败: {}", e.getMessage());
        }

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

        // 写入 Redis 缓存（10分钟过期，购物车变化频繁用短缓存）
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入购物车推荐缓存失败: {}", e.getMessage());
        }

        saveExposureRecords(userId, "product", "content", result);
        return result;
    }

    // ======================== 跨模块联动（8.3） ========================

    /**
     * 🆕 8.3.1 英文机器标签 → 中文课程标签 映射
     *
     * Astrometry.net 返回的 machine_tags 全部是英文（如 "nebula"、"galaxy"），
     * 而课程/商品库里的 tags 是中文（如 "星云摄影"、"星系观测"）。
     * 为了让「AI识别→推荐课程」能跑通，在这里静态映射 8~10 个高频天体类型。
     *
     * 📌 设计考量:
     * - 一个英文标签可映射多个中文候选关键字（LIKE 模糊匹配，命中任一即可）
     * - 大小写不敏感：查询前会 toLowerCase
     * - 未命中的英文标签直接丢弃，不做错误兜底
     * - 如果整个 machine_tags 解析后为空或全部未命中，返回空让上层走热门兜底
     */
    private static final Map<String, List<String>> EN_TO_ZH_TAG_MAPPING = new HashMap<String, List<String>>() {{
        // 星云类（弥漫星云、行星状星云、发射星云等）
        put("nebula", Arrays.asList("星云", "深空"));
        put("emission nebula", Arrays.asList("星云", "深空"));
        put("planetary nebula", Arrays.asList("星云", "深空"));
        // 星系类
        put("galaxy", Arrays.asList("星系", "深空"));
        // 行星
        put("planet", Arrays.asList("行星", "太阳系"));
        // 恒星 / 星团
        put("star", Arrays.asList("恒星", "星座"));
        put("star cluster", Arrays.asList("星团", "深空"));
        put("cluster", Arrays.asList("星团", "深空"));
        // 月亮 / 太阳 / 彗星 / 小行星
        put("moon", Arrays.asList("月球", "月亮"));
        put("sun", Arrays.asList("太阳", "日面"));
        put("comet", Arrays.asList("彗星"));
        put("asteroid", Arrays.asList("小行星", "太阳系"));
    }};

    /**
     * 🆕 8.3.1  AI识别 → 推荐相关课程
     *
     * 📌 算法流程:
     *   1. 查 tb_recognition.machine_tags（由 Astrometry.net 异步回写）
     *   2. 英文标签 → 中文关键字（通过 EN_TO_ZH_TAG_MAPPING 映射）
     *   3. 用 LIKE '%关键字%' 查 tb_course.tags（status=1 已发布）
     *   4. Java 侧过滤掉用户已学过的课程（tb_course_progress 有记录）
     *   5. 按 view_count 倒序（CourseMapper.getRecommendByTags 已处理）
     *   6. 三级兜底: 机器标签为空/全部未命中 → 热门课程；命中不足 → 热门补齐
     *
     * 📌 返回 List<Object> 以兼容 Controller 签名；内部实际是 List<CourseVO>
     *
     * @param recognitionId  识别记录 ID
     * @param limit          返回课程数上限
     */
    @Override
    public List<Object> getRecognitionCourseRecommend(Long recognitionId, int limit) {
        if (recognitionId == null || limit <= 0) {
            return Collections.emptyList();
        }

        // 1. 查识别记录
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        if (recognition == null || recognition.getStatus() == null || recognition.getStatus() != 1) {
            log.debug("[8.3.1] 识别记录不存在或未成功, id={}", recognitionId);
            return Collections.emptyList();
        }
        Long userId = recognition.getUserId();  // 可能为 null（游客）

        // 2. 解析 machine_tags 并映射为中文关键字
        //    🔧 2026-04-16 修复: 由精确匹配改为子串包含匹配
        //    原因: Astrometry.net 返回的 machine_tags 常带限定词，如 "Andromeda Galaxy"/"emission nebula"，
        //          精确匹配 "galaxy"/"nebula" 会全部 miss，导致永远走不到标签推荐分支。
        Set<String> machineTags = parseTags(recognition.getMachineTags());
        Set<String> zhKeywords = new LinkedHashSet<>();
        for (String tag : machineTags) {
            if (tag == null) continue;
            String lowerTag = tag.toLowerCase().trim();
            for (Map.Entry<String, List<String>> entry : EN_TO_ZH_TAG_MAPPING.entrySet()) {
                // 子串包含即命中（"andromeda galaxy".contains("galaxy") = true）
                if (lowerTag.contains(entry.getKey())) {
                    zhKeywords.addAll(entry.getValue());
                }
            }
        }
        log.debug("[8.3.1] recognitionId={} machineTags={} → zhKeywords={}",
                recognitionId, machineTags, zhKeywords);

        // 3. 收集该用户已学课程 ID（用于 Java 侧排除）
        Set<Long> learnedCourseIds = getLearnedCourseIds(userId);

        List<CourseVO> matched = Collections.emptyList();
        // 4. 机器标签有映射 → 走标签推荐
        if (!zhKeywords.isEmpty()) {
            // 多取一些冗余数据（limit*3），后续 Java 过滤/截断
            List<CourseVO> raw = courseMapper.getRecommendByTags(userId,
                    new ArrayList<>(zhKeywords), limit * 3);
            matched = raw.stream()
                    .filter(vo -> !learnedCourseIds.contains(vo.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // 5. 命中不足 → 用热门课程补齐（排除已入选 + 已学）
        //    🔧 2026-04-16 修复: 从 getHotCourses(userId, limit*2) 改为取固定 Math.max(limit*10, 20) 条
        //    原因: 活跃用户已学课程多（如用户 10001 已学 29 门，全库 51 门），
        //          limit*2 仅取 Top2 热门，容易全被已学过滤掉 → 兜底返回空 → 不发通知
        if (matched.size() < limit) {
            Set<Long> existIds = matched.stream().map(CourseVO::getId).collect(Collectors.toSet());
            existIds.addAll(learnedCourseIds);
            int poolSize = Math.max(limit * 10, 20);  // 至少拿 20 门候选兜底
            List<CourseVO> hot = courseMapper.getHotCourses(userId, poolSize).stream()
                    .filter(vo -> !existIds.contains(vo.getId()))
                    .limit(limit - matched.size())
                    .collect(Collectors.toList());
            List<CourseVO> merged = new ArrayList<>(matched);
            merged.addAll(hot);
            matched = merged;
        }

        log.info("[8.3.1] recognitionId={} userId={} 最终推荐 {} 门课程",
                recognitionId, userId, matched.size());

        // 6. 曝光记录（List<Object>，saveExposureRecords 里对非 Product/Post 类型静默跳过）
        return new ArrayList<>(matched);
    }

    /**
     * 🆕 8.3.2  完课 → 推荐下一门课程
     *
     * 📌 算法流程:
     *   1. 查当前课程 tags
     *   2. 用当前课程 tags 去 LIKE 匹配候选课程（排除自身 + 排除已学）
     *   3. 在 Java 侧用 Jaccard 相似度对候选排序（与当前课程标签集越接近越靠前）
     *   4. 不足时用热门课程兜底
     *
     * 📌 为什么二次用 Jaccard:
     *   DB LIKE 只能判断"命中/不命中"，多标签候选无法体现"和目标课程多像"。
     *   Jaccard 在 Java 侧做精排，既保持 SQL 简单，又能给论文提供算法亮点。
     *
     * @param userId    当前登录用户 ID（需鉴权，必定非空）
     * @param courseId  当前已完成的课程 ID
     * @param limit     返回数量上限
     */
    @Override
    public List<Object> getNextCourseRecommend(Long userId, Long courseId, int limit) {
        if (userId == null || courseId == null || limit <= 0) {
            return Collections.emptyList();
        }

        // 1. 查当前课程
        Course current = courseMapper.selectById(courseId);
        if (current == null || current.getStatus() == null || current.getStatus() != 1) {
            log.debug("[8.3.2] 当前课程不存在或未发布, id={}", courseId);
            return Collections.emptyList();
        }

        // 2. 已学课程集合（包含当前课程）
        Set<Long> learnedCourseIds = getLearnedCourseIds(userId);
        learnedCourseIds.add(courseId);  // 即使完成记录还未写 DB，也要排除

        // 3. 解析当前课程 tags → 作为候选匹配的标签池
        Set<String> currentTags = parseTags(current.getTags());

        List<CourseVO> ranked = Collections.emptyList();
        if (!currentTags.isEmpty()) {
            // 用 tags LIKE 粗召回（放大到 limit*4，给 Jaccard 精排留余量）
            List<CourseVO> candidates = courseMapper.getRecommendByTags(userId,
                    new ArrayList<>(currentTags), limit * 4);

            // Java 侧: 排除已学 + 用 Jaccard 精排
            final String currentTagsJson = current.getTags();
            ranked = candidates.stream()
                    .filter(vo -> !learnedCourseIds.contains(vo.getId()))
                    .sorted((a, b) -> {
                        double simA = jaccardSimilarity(currentTagsJson, a.getTags());
                        double simB = jaccardSimilarity(currentTagsJson, b.getTags());
                        return Double.compare(simB, simA);  // 降序
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // 4. 不足时热门兜底
        if (ranked.size() < limit) {
            Set<Long> existIds = ranked.stream().map(CourseVO::getId).collect(Collectors.toSet());
            existIds.addAll(learnedCourseIds);
            List<CourseVO> hot = courseMapper.getHotCourses(userId, limit * 2).stream()
                    .filter(vo -> !existIds.contains(vo.getId()))
                    .limit(limit - ranked.size())
                    .collect(Collectors.toList());
            List<CourseVO> merged = new ArrayList<>(ranked);
            merged.addAll(hot);
            ranked = merged;
        }

        log.info("[8.3.2] userId={} courseId={} 推荐下一门课程 {} 条",
                userId, courseId, ranked.size());

        return new ArrayList<>(ranked);
    }

    /**
     * 🆕 8.3.3  签到观测点 → 推荐适合器材
     *
     * 📌 算法流程（论文可描述为「规则前置过滤 + 内容相似度排序」）:
     *
     *   Step 1: 根据观测点物理条件生成目标标签池
     *     - altitude > 2000m && lightPollutionLevel ≤ 3  →  深空摄影器材（黑暗高海拔）
     *     - altitude > 1000m                             →  深空 / 便携组合
     *     - 其余                                          →  入门 / 便携 / 月球/行星
     *
     *   Step 2: 用 selectByTagsAny 召回候选商品（放大到 limit*3）
     *
     *   Step 3: 综合排序 = Jaccard(tags, 目标标签) × 0.6 + 归一化销量 × 0.4
     *
     *   Step 4: Redis 缓存 30 分钟（观测点物理属性几乎不变，可放心缓存）
     *
     *   Step 5: 无命中则走热门商品兜底
     *
     * @param spotId 观测点 ID
     * @param limit  返回上限
     */
    @Override
    public List<RecommendProductVO> getSpotEquipmentRecommend(Long spotId, int limit) {
        if (spotId == null || limit <= 0) {
            return Collections.emptyList();
        }

        // Redis 缓存: 30 分钟（观测点物理属性基本恒定）
        String cacheKey = "recommend:spot:equipment:" + spotId + ":" + limit;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                List<RecommendProductVO> list = JSON.parseArray(cached, RecommendProductVO.class);
                if (list != null) {
                    log.debug("[8.3.3] Redis 命中 spotId={} limit={}", spotId, limit);
                    return list;
                }
            }
        } catch (Exception e) {
            log.warn("[8.3.3] Redis 读取失败: {}", e.getMessage());
        }

        // 1. 查观测点
        ObservationSpot spot = observationSpotMapper.selectById(spotId);
        if (spot == null) {
            log.debug("[8.3.3] 观测点不存在 spotId={}", spotId);
            return Collections.emptyList();
        }

        // 2. 规则前置过滤: 根据海拔 + 光污染等级生成目标标签池
        List<String> targetTags = buildSpotTargetTags(spot);
        log.debug("[8.3.3] spotId={} altitude={} lightLv={} → targetTags={}",
                spotId, spot.getAltitude(), spot.getLightPollutionLevel(), targetTags);

        // 3. 粗召回
        List<Product> candidates = selectByTagsAny(targetTags, limit * 3);

        List<RecommendProductVO> result;
        if (candidates.isEmpty()) {
            // 无命中 → 热门商品兜底
            log.info("[8.3.3] spotId={} 标签无命中，走热门兜底", spotId);
            result = selectHotProducts(limit).stream()
                    .map(p -> toProductVO(p, "热门推荐", "hot"))
                    .collect(Collectors.toList());
        } else {
            // 4. 归一化销量（Min-Max）
            int maxSales = candidates.stream()
                    .mapToInt(p -> p.getSales() == null ? 0 : p.getSales())
                    .max().orElse(0);
            final double maxSalesD = maxSales == 0 ? 1.0 : maxSales;

            // 5. 综合评分: Jaccard × 0.6 + 归一化销量 × 0.4
            String targetTagsJson = JSON.toJSONString(targetTags);
            result = candidates.stream()
                    .map(p -> {
                        double jaccard = jaccardSimilarity(targetTagsJson, p.getTags());
                        double normSales = (p.getSales() == null ? 0 : p.getSales()) / maxSalesD;
                        double score = jaccard * 0.6 + normSales * 0.4;

                        RecommendProductVO vo = toProductVO(p, "适合该观测点", "content");
                        vo.setScore(score);
                        return vo;
                    })
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // 6. 写缓存（优雅降级）
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[8.3.3] Redis 写入失败: {}", e.getMessage());
        }

        log.info("[8.3.3] spotId={} 推荐器材 {} 件", spotId, result.size());
        return result;
    }

    /**
     * 🆕 8.3 辅助: 查询用户已学过的课程 ID 集合
     * （userId 为 null 时返回空集合，用于识别模块的游客场景）
     */
    private Set<Long> getLearnedCourseIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        try {
            LambdaQueryWrapper<CourseProgress> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CourseProgress::getUserId, userId)
                    .select(CourseProgress::getCourseId);
            List<CourseProgress> list = courseProgressMapper.selectList(wrapper);
            return list.stream()
                    .map(CourseProgress::getCourseId)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("[8.3] 查询用户已学课程失败 userId={}: {}", userId, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 🆕 8.3.3 辅助: 根据观测点物理属性生成目标标签池
     *
     * 规则说明:
     *   海拔 > 2000m 且 光污染 ≤ 3 (Bortle 1-3 暗天) → 专业深空摄影场景
     *   海拔 > 1000m                               → 深空 + 便携组合
     *   其他（市区/郊区/低海拔）                       → 入门 / 便携 / 月面行星观测
     *
     * @return 候选标签列表（用于 LIKE 召回）
     */
    private List<String> buildSpotTargetTags(ObservationSpot spot) {
        int altitude = spot.getAltitude() == null ? 0 : spot.getAltitude();
        int lightLv = spot.getLightPollutionLevel() == null ? 9 : spot.getLightPollutionLevel();

        if (altitude > 2000 && lightLv <= 3) {
            // 高海拔 + 极暗天：适合深空摄影
            return Arrays.asList("深空摄影", "天文相机", "赤道仪", "滤镜", "星云", "星系");
        } else if (altitude > 1000) {
            // 中海拔：深空 + 便携
            return Arrays.asList("深空", "便携", "望远镜", "双筒", "追星");
        } else {
            // 城市/郊区：入门为主
            return Arrays.asList("入门", "便携", "双筒", "月球", "行星", "目镜");
        }
    }

    /**
     * 帖子个性化推荐（论坛"推荐"Tab，瀑布流分页）
     *
     * 📌 算法（重构 v8.58 final）:
     * ✅ 核心思想：【召回全部 + 打分排序 + 永不过滤】
     *   像小红书/抖音一样，推荐页必须把 pageSize 填满，不会因"浏览过""自己发的"而留白。
     *
     * 步骤:
     * 1. 召回：所有 status=2 已发布帖子（不排除自己、不排除已浏览的，只是后续降权）
     * 2. 评分：score = α·jaccard + β·normHot + γ·freshness - 惩罚项
     *     - jaccard     : 与用户画像 tag 交集（有画像时生效）
     *     - normHot     : hot_score / max(hot_score)，归一化 [0,1]
     *     - freshness   : 越新越高（指数衰减，30 天半衰期）
     *     - 已浏览惩罚  : -0.25（让它们排后面但仍展示）
     *     - 自己帖轻惩  : -0.05
     * 3. 排序：score DESC 全量排序
     * 4. 分页：pageNum/pageSize 切片
     * 5. 无画像（未登录/无浏览/无 interest_tags）：纯热度 + 时效排序
     *
     * 📌 权重:
     *   有画像: jaccard*0.5 + normHot*0.3 + freshness*0.2
     *   无画像: normHot*0.7 + freshness*0.3
     */
    @Override
    public Map<String, Object> getPostRecommend(Long userId, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 50;

        // 1. 构建用户画像（tag 集合）+ 已浏览帖子 ID 集合（用于降权）
        Set<String> userTagProfile = new HashSet<>();
        Set<Long> recentPostIdSet = new HashSet<>();
        if (userId != null) {
            List<Long> recentPostIds = postBrowseLogMapper.selectRecentPostIds(userId, 30);
            if (!recentPostIds.isEmpty()) {
                recentPostIdSet.addAll(recentPostIds);
                List<Post> recentPosts = postMapper.selectBatchIds(recentPostIds);
                for (Post p : recentPosts) {
                    userTagProfile.addAll(parseTags(p.getTags()));
                }
            }
            // 浏览历史 tags 为空，回退到 interest_tags 冷启动画像
            if (userTagProfile.isEmpty()) {
                User user = userMapper.selectById(userId);
                if (user != null && user.getInterestTags() != null) {
                    userTagProfile.addAll(parseTags(user.getInterestTags()));
                }
            }
        }

        // 2. 召回全部 status=2 帖子（不做任何排除，只降权）
        //    限 1000 条上限，避免全表排序；瀑布流真实场景 1000 条远够用
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 2).last("LIMIT 1000");
        List<Post> candidates = postMapper.selectList(wrapper);

        // 3. 归一化因子计算
        double maxHot = 0.0;
        for (Post p : candidates) {
            if (p.getHotScore() != null && p.getHotScore() > maxHot) maxHot = p.getHotScore();
        }
        if (maxHot <= 0) maxHot = 1.0;

        final double maxHotFinal = maxHot;
        final String userTagsJson = userTagProfile.isEmpty() ? null
                : JSON.toJSONString(new ArrayList<>(userTagProfile));
        final boolean hasProfile = userTagsJson != null;
        final long nowMs = System.currentTimeMillis();
        final long halfLifeMs = 30L * 24 * 3600 * 1000; // 30 天半衰期
        final Long currentUserId = userId;

        // 4. 评分 + 排序
        List<RecommendPostVO> allScored = candidates.stream()
                .map(post -> {
                    // (a) 热度归一化
                    double normHot = (post.getHotScore() == null ? 0.0 : post.getHotScore()) / maxHotFinal;

                    // (b) 时效性：半衰期 30 天的指数衰减
                    double freshness = 1.0;
                    if (post.getCreateTime() != null) {
                        long ageMs = nowMs - post.getCreateTime()
                                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                        if (ageMs > 0) {
                            freshness = Math.pow(0.5, (double) ageMs / halfLifeMs);
                        }
                    }

                    // (c) 内容相似度（有画像时才算）
                    double jaccard = hasProfile ? postContentSimilarity(userTagsJson, post.getTags()) : 0.0;

                    // (d) 综合分
                    double score = hasProfile
                            ? (jaccard * 0.5 + normHot * 0.3 + freshness * 0.2)
                            : (normHot * 0.7 + freshness * 0.3);

                    // (e) 降权项：已浏览 -0.25，自己发的 -0.05
                    if (recentPostIdSet.contains(post.getId())) score -= 0.25;
                    if (currentUserId != null && currentUserId.equals(post.getUserId())) score -= 0.05;

                    RecommendPostVO vo = toPostVO(post, hasProfile ? "content" : "hot");
                    vo.setScore(score);
                    return vo;
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());

        long total = allScored.size();

        // 5. 分页切片
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, allScored.size());
        List<RecommendPostVO> pageList = from >= allScored.size()
                ? Collections.emptyList()
                : allScored.subList(from, to);

        // 仅第一页曝光埋点，避免分页重复
        if (userId != null && pageNum == 1 && !pageList.isEmpty()) {
            saveExposureRecords(userId, "post", hasProfile ? "content" : "hot", pageList);
        }

        log.info("[8.3.4] userId={} pageNum={} pageSize={} 候选={} 返回={} 总数={} hasProfile={}",
                userId, pageNum, pageSize, candidates.size(), pageList.size(), total, hasProfile);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
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
     * 解析标签字符串为 Set
     * 兼容两种历史格式：
     *   1. JSON 数组:       ["深空摄影","天文相机"]
     *   2. 分隔符字符串:    "深空摄影,天文相机,窄带滤镜"（中/英文逗号、顿号）
     * 任何异常下返回空集合，保证推荐算法可降级运行
     */
    private Set<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.trim().isEmpty() || "[]".equals(tagsJson.trim())) {
            return Collections.emptySet();
        }
        String trimmed = tagsJson.trim();
        // 仅当以 [ 开头时才尝试 JSON 解析，避免普通 CSV 字符串触发解析异常
        if (trimmed.startsWith("[")) {
            try {
                List<String> list = JSON.parseArray(trimmed, String.class);
                Set<String> set = new HashSet<>();
                for (String s : list) {
                    if (s != null && !s.trim().isEmpty()) {
                        set.add(s.trim());
                    }
                }
                return set;
            } catch (Exception e) {
                log.debug("标签JSON解析失败, 尝试按分隔符拆分: {}", trimmed);
                // 继续走下方 CSV 兜底
            }
        }
        // CSV / 顿号 兜底：按中英文逗号、顿号切分
        Set<String> set = new HashSet<>();
        for (String s : trimmed.split("[,，、]")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                set.add(t);
            }
        }
        return set;
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

package com.astronomy.mall.module.recognition.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.recognition.entity.Recognition;
import com.astronomy.mall.module.recognition.mapper.RecognitionMapper;
import com.astronomy.mall.module.recognition.service.RecognitionRecommendService;
import com.astronomy.mall.module.recognition.vo.RecognitionProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI星图识别 - 器材推荐 Service 实现类
 *
 * 📌 推荐流程:
 *   1. 读取 tb_recognition.machine_tags（JSON数组）
 *   2. 遍历每个 tag，通过 TAG_MAPPING 获取商品标签关键词列表
 *   3. 用关键词在 tb_product.tags 中做 LIKE 模糊匹配（status=1, deleted=0）
 *   4. 去重后取前6个，写回 tb_recognition.recommended_products
 *   5. 无匹配时兜底：按 sales 倒序取热销前6个
 *
 * 📌 数据库查询方式:
 *   使用 JdbcTemplate 直接查 tb_product，避免在 recognition 模块引入 product 模块的 Mapper 依赖。
 *
 * @since 4.4 (2026-03-16)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecognitionRecommendServiceImpl implements RecognitionRecommendService {

    private final RecognitionMapper recognitionMapper;
    private final JdbcTemplate      jdbcTemplate;

    /** 最多返回的推荐商品数量 */
    private static final int MAX_RECOMMEND = 6;

    // =============================================
    // ⭐ machine_tag → 商品标签关键词映射表
    // Key:   Astrometry machine_tag（小写）
    // Value: tb_product.tags 中匹配的关键词列表
    // =============================================
    private static final Map<String, List<String>> TAG_MAPPING = new LinkedHashMap<>();

    static {
        // nebula: 星云 → 深空摄影器材
        TAG_MAPPING.put("nebula",        Arrays.asList("深空摄影", "天文相机", "窄带滤镜", "CCD"));

        // galaxy: 星系 → 大口径 + 深空摄影
        TAG_MAPPING.put("galaxy",        Arrays.asList("深空摄影", "大口径望远镜", "天文相机"));

        // planet: 行星 → 高倍观测器材
        TAG_MAPPING.put("planet",        Arrays.asList("行星观测", "高倍目镜", "巴洛镜", "行星相机"));

        // star cluster: 疏散/球状星团 → 双筒+广角
        TAG_MAPPING.put("star cluster",  Arrays.asList("双筒望远镜", "广角目镜", "寻星镜"));
        TAG_MAPPING.put("open cluster",  Arrays.asList("双筒望远镜", "广角目镜"));
        TAG_MAPPING.put("globular cluster", Arrays.asList("大口径望远镜", "高倍目镜"));

        // moon: 月面 → 滤镜+月面摄影
        TAG_MAPPING.put("moon",          Arrays.asList("月面摄影", "滤镜", "月球滤镜"));

        // comet: 彗星 → 广角追踪
        TAG_MAPPING.put("comet",         Arrays.asList("广角望远镜", "赤道仪", "追踪"));

        // star: 恒星 → 通用入门
        TAG_MAPPING.put("star",          Arrays.asList("入门望远镜", "寻星镜"));

        // emission/reflection: 特定星云类型
        TAG_MAPPING.put("emission",      Arrays.asList("窄带滤镜", "Ha滤镜", "深空摄影"));
        TAG_MAPPING.put("reflection",    Arrays.asList("天文相机", "深空摄影"));

        // 通用兜底关键词（TAG_MAPPING无匹配时不用，直接走销量兜底）
    }

    // =============================================
    // ⭐ machine_tag → 推荐理由文案
    // =============================================
    private static final Map<String, String> TAG_REASON_MAP = new HashMap<>();

    static {
        TAG_REASON_MAP.put("nebula",           "适合深空星云摄影");
        TAG_REASON_MAP.put("galaxy",           "适合深空星系观测");
        TAG_REASON_MAP.put("planet",           "适合行星高倍观测");
        TAG_REASON_MAP.put("star cluster",     "适合疏散星团观测");
        TAG_REASON_MAP.put("open cluster",     "适合疏散星团观测");
        TAG_REASON_MAP.put("globular cluster", "适合球状星团观测");
        TAG_REASON_MAP.put("moon",             "适合月面观测摄影");
        TAG_REASON_MAP.put("comet",            "适合彗星广角追踪");
        TAG_REASON_MAP.put("star",             "天文入门推荐");
        TAG_REASON_MAP.put("emission",         "适合发射星云摄影");
        TAG_REASON_MAP.put("reflection",       "适合反射星云摄影");
    }

    // =============================================
    // 核心实现
    // =============================================

    @Override
    public List<RecognitionProductVO> getRecommend(Long recognitionId, Long userId) {

        // 1. 查询并鉴权
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        if (recognition == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "识别记录不存在");
        }
        if (!recognition.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看此识别记录");
        }

        // 2. 如果已有缓存推荐结果，直接用缓存（避免重复计算）
        if (StringUtils.hasText(recognition.getRecommendedProducts())) {
            try {
                List<Long> cachedIds = JSON.parseArray(recognition.getRecommendedProducts(), Long.class);
                if (cachedIds != null && !cachedIds.isEmpty()) {
                    List<RecognitionProductVO> cached = queryProductsByIds(cachedIds, "已为您推荐");
                    if (!cached.isEmpty()) {
                        log.debug("[Recommend] 使用缓存推荐结果, recognitionId={}", recognitionId);
                        return cached;
                    }
                }
            } catch (Exception e) {
                log.warn("[Recommend] 解析缓存推荐结果失败, recognitionId={}", recognitionId);
            }
        }

        // 3. 解析 machine_tags
        List<String> machineTags = parseMachineTags(recognition.getMachineTags());
        log.info("[Recommend] recognitionId={}, machineTags={}", recognitionId, machineTags);

        // 4. 通过 TAG_MAPPING 收集商品标签关键词
        List<RecognitionProductVO> products = new ArrayList<>();
        String matchedTag = null; // 记录首个成功匹配的tag，用于生成推荐理由

        if (!machineTags.isEmpty()) {
            // 按 TAG_MAPPING 顺序遍历，保证优先级
            for (String tag : machineTags) {
                String tagLower = tag.toLowerCase().trim();
                List<String> keywords = TAG_MAPPING.get(tagLower);
                if (keywords == null) continue;

                if (matchedTag == null) matchedTag = tagLower;

                // 用每个关键词查商品
                for (String keyword : keywords) {
                    if (products.size() >= MAX_RECOMMEND) break;
                    List<RecognitionProductVO> matched = queryProductsByTagKeyword(keyword, MAX_RECOMMEND - products.size());
                    // 去重追加
                    Set<Long> existingIds = products.stream().map(RecognitionProductVO::getId).collect(Collectors.toSet());
                    for (RecognitionProductVO p : matched) {
                        if (!existingIds.contains(p.getId())) {
                            p.setReason(TAG_REASON_MAP.getOrDefault(tagLower, "推荐观测器材"));
                            products.add(p);
                            existingIds.add(p.getId());
                        }
                    }
                }

                if (products.size() >= MAX_RECOMMEND) break;
            }
        }

        // 5. 无匹配时兜底：热销前6
        if (products.isEmpty()) {
            log.info("[Recommend] 无标签匹配，使用热销兜底, recognitionId={}", recognitionId);
            products = queryHotProducts(MAX_RECOMMEND);
            products.forEach(p -> p.setReason("热销推荐"));
        }

        // 6. 截取前6个
        if (products.size() > MAX_RECOMMEND) {
            products = products.subList(0, MAX_RECOMMEND);
        }

        // 7. 写回 tb_recognition.recommended_products（异步写，不阻塞返回）
        if (!products.isEmpty()) {
            try {
                List<Long> productIds = products.stream()
                        .map(RecognitionProductVO::getId)
                        .collect(Collectors.toList());
                String idsJson = JSON.toJSONString(productIds);
                jdbcTemplate.update(
                        "UPDATE tb_recognition SET recommended_products = ? WHERE id = ?",
                        idsJson, recognitionId
                );
                log.info("[Recommend] 写回推荐结果, recognitionId={}, ids={}", recognitionId, idsJson);
            } catch (Exception e) {
                // 写回失败不影响返回
                log.warn("[Recommend] 写回推荐结果失败, recognitionId={}", recognitionId, e);
            }
        }

        return products;
    }

    // =============================================
    // 私有查询方法
    // =============================================

    /**
     * 按标签关键词查询商品（LIKE匹配）
     * 只查上架且未删除的商品，按销量降序
     */
    private List<RecognitionProductVO> queryProductsByTagKeyword(String keyword, int limit) {
        String sql = "SELECT id, product_name, main_image, price, sales " +
                "FROM tb_product " +
                "WHERE status = 1 AND deleted = 0 AND tags LIKE ? " +
                "ORDER BY sales DESC " +
                "LIMIT ?";
        try {
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> RecognitionProductVO.builder()
                            .id(rs.getLong("id"))
                            .productName(rs.getString("product_name"))
                            .mainImage(rs.getString("main_image"))
                            .price(rs.getBigDecimal("price"))
                            .salesCount(rs.getInt("sales"))
                            .build(),
                    "%" + keyword + "%", limit);
        } catch (Exception e) {
            log.warn("[Recommend] 查询商品失败, keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 按商品ID列表查询（命中缓存时使用）
     */
    private List<RecognitionProductVO> queryProductsByIds(List<Long> ids, String reason) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, product_name, main_image, price, sales " +
                "FROM tb_product " +
                "WHERE id IN (" + placeholders + ") AND status = 1 AND deleted = 0";
        try {
            Object[] params = ids.toArray();
            return jdbcTemplate.query(sql, params,
                    (rs, rowNum) -> RecognitionProductVO.builder()
                            .id(rs.getLong("id"))
                            .productName(rs.getString("product_name"))
                            .mainImage(rs.getString("main_image"))
                            .price(rs.getBigDecimal("price"))
                            .salesCount(rs.getInt("sales"))
                            .reason(reason)
                            .build());
        } catch (Exception e) {
            log.warn("[Recommend] 按ID查询商品失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 兜底：查热销前N个商品
     */
    private List<RecognitionProductVO> queryHotProducts(int limit) {
        String sql = "SELECT id, product_name, main_image, price, sales " +
                "FROM tb_product " +
                "WHERE status = 1 AND deleted = 0 " +
                "ORDER BY sales DESC " +
                "LIMIT ?";
        try {
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> RecognitionProductVO.builder()
                            .id(rs.getLong("id"))
                            .productName(rs.getString("product_name"))
                            .mainImage(rs.getString("main_image"))
                            .price(rs.getBigDecimal("price"))
                            .salesCount(rs.getInt("sales"))
                            .build(),
                    limit);
        } catch (Exception e) {
            log.warn("[Recommend] 查询热销商品失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 安全解析 machine_tags JSON 数组
     */
    private List<String> parseMachineTags(String jsonStr) {
        if (!StringUtils.hasText(jsonStr)) return Collections.emptyList();
        try {
            List<String> tags = JSON.parseArray(jsonStr, String.class);
            return tags != null ? tags : Collections.emptyList();
        } catch (Exception e) {
            log.warn("[Recommend] 解析 machine_tags 失败: {}", jsonStr);
            return Collections.emptyList();
        }
    }
}
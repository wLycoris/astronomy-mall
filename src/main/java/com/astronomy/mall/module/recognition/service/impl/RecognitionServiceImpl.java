package com.astronomy.mall.module.recognition.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.recognition.dto.SubmitRecognitionDTO;
import com.astronomy.mall.module.recognition.entity.Recognition;
import com.astronomy.mall.module.recognition.mapper.RecognitionMapper;
import com.astronomy.mall.module.recognition.service.RecognitionService;
import com.astronomy.mall.module.recognition.service.external.AstrometryService;
import com.astronomy.mall.module.recognition.vo.RecognitionStatsVO;
import com.astronomy.mall.module.recognition.vo.RecognitionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI星图识别业务服务实现类
 *
 * 📌 v4.1: submit, getStatus
 * 📌 v4.2: getDetail, getHistory
 * 📌 v4.3: getResult（含中英文天体名称映射 + 坐标格式化）
 * 📌 v4.5: deleteRecord, getStats；getHistory 升级支持 status 筛选 + 附加 hasImage/mainObjects
 *
 * v4.3 新增内容:
 *   - CELESTIAL_NAME_MAP  : 50+天体中英文静态映射表
 *   - CELESTIAL_TYPE_MAP  : 天体类型分类（nebula/galaxy/cluster/constellation）
 *   - getResult()         : 返回含格式化字段的完整VO
 *   - formatRA()          : 赤经度数 → 时分秒格式化
 *   - formatDec()         : 赤纬度数 → 度分秒格式化
 *   - formatRadius()      : 视野半径格式化
 *   - buildCelestialObjects(): 英文名 → CelestialObjectVO列表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecognitionServiceImpl implements RecognitionService {

    private final RecognitionMapper recognitionMapper;
    private final AstrometryService astrometryService;

    // =============================================
    // ⭐ v4.3 新增：天体中英文名称映射表（约55个常见天体）
    // 来源: IAU/NASA官方名称 → 中文天文学会标准译名
    // =============================================
    private static final Map<String, String> CELESTIAL_NAME_MAP = new HashMap<>();

    static {
        // ---- 星云 (Nebula) ----
        CELESTIAL_NAME_MAP.put("Orion Nebula",           "猎户座大星云");
        CELESTIAL_NAME_MAP.put("Crab Nebula",            "蟹状星云");
        CELESTIAL_NAME_MAP.put("Eagle Nebula",           "鹰状星云");
        CELESTIAL_NAME_MAP.put("Lagoon Nebula",          "礁湖星云");
        CELESTIAL_NAME_MAP.put("Trifid Nebula",          "三叶星云");
        CELESTIAL_NAME_MAP.put("Omega Nebula",           "天鹅星云");
        CELESTIAL_NAME_MAP.put("Ring Nebula",            "环状星云");
        CELESTIAL_NAME_MAP.put("Dumbbell Nebula",        "哑铃星云");
        CELESTIAL_NAME_MAP.put("Helix Nebula",           "螺旋星云");
        CELESTIAL_NAME_MAP.put("Horsehead Nebula",       "马头星云");
        CELESTIAL_NAME_MAP.put("Flame Nebula",           "火焰星云");
        CELESTIAL_NAME_MAP.put("California Nebula",      "加利福尼亚星云");
        CELESTIAL_NAME_MAP.put("Rosette Nebula",         "玫瑰星云");
        CELESTIAL_NAME_MAP.put("Veil Nebula",            "面纱星云");
        CELESTIAL_NAME_MAP.put("Bubble Nebula",          "气泡星云");
        CELESTIAL_NAME_MAP.put("Wizard Nebula",          "巫师星云");
        CELESTIAL_NAME_MAP.put("Cone Nebula",            "锥状星云");
        CELESTIAL_NAME_MAP.put("North America Nebula",   "北美洲星云");
        CELESTIAL_NAME_MAP.put("Eta Carinae Nebula",     "船底座星云");
        CELESTIAL_NAME_MAP.put("Tarantula Nebula",       "蜘蛛星云");
        CELESTIAL_NAME_MAP.put("Running Man Nebula",     "跑步人星云");
        CELESTIAL_NAME_MAP.put("Witch Head Nebula",      "女巫头星云");
        CELESTIAL_NAME_MAP.put("Cygnus Loop",            "天鹅座超新星遗迹");
        CELESTIAL_NAME_MAP.put("Pillars of Creation",    "创生之柱");
        CELESTIAL_NAME_MAP.put("Thor's Helmet",          "雷神头盔星云");
        CELESTIAL_NAME_MAP.put("Pac-Man Nebula",         "吃豆人星云");
        CELESTIAL_NAME_MAP.put("Heart Nebula",           "心形星云");
        CELESTIAL_NAME_MAP.put("Soul Nebula",            "灵魂星云");
        CELESTIAL_NAME_MAP.put("IC 1805",                "心脏星云");
        CELESTIAL_NAME_MAP.put("IC 1848",                "灵魂星云");
        CELESTIAL_NAME_MAP.put("Cassiopeia A",           "仙后座A超新星遗迹");
        CELESTIAL_NAME_MAP.put("Pelican Nebula",         "鹈鹕星云");
        CELESTIAL_NAME_MAP.put("Cat's Eye Nebula",       "猫眼星云");

        // ---- 星系 (Galaxy) ----
        CELESTIAL_NAME_MAP.put("Andromeda Galaxy",       "仙女座星系");
        CELESTIAL_NAME_MAP.put("Triangulum Galaxy",      "三角座星系");
        CELESTIAL_NAME_MAP.put("Whirlpool Galaxy",       "涡状星系");
        CELESTIAL_NAME_MAP.put("Sombrero Galaxy",        "草帽星系");
        CELESTIAL_NAME_MAP.put("Black Eye Galaxy",       "黑眼星系");
        CELESTIAL_NAME_MAP.put("Pinwheel Galaxy",        "风车星系");
        CELESTIAL_NAME_MAP.put("Sunflower Galaxy",       "向日葵星系");
        CELESTIAL_NAME_MAP.put("Bode's Galaxy",          "波德星系");
        CELESTIAL_NAME_MAP.put("Cigar Galaxy",           "雪茄星系");
        CELESTIAL_NAME_MAP.put("Large Magellanic Cloud", "大麦哲伦星云");
        CELESTIAL_NAME_MAP.put("Small Magellanic Cloud", "小麦哲伦星云");

        // ---- 星团 (Cluster) ----
        CELESTIAL_NAME_MAP.put("Pleiades",               "昴星团");
        CELESTIAL_NAME_MAP.put("Hyades",                 "毕星团");
        CELESTIAL_NAME_MAP.put("Beehive Cluster",        "蜂巢星团");
        CELESTIAL_NAME_MAP.put("Double Cluster",         "双星团");
        CELESTIAL_NAME_MAP.put("Jewel Box Cluster",      "珠宝盒星团");
        CELESTIAL_NAME_MAP.put("Wild Duck Cluster",      "野鸭星团");
        CELESTIAL_NAME_MAP.put("Omega Centauri",         "半人马座欧米伽球状星团");
        CELESTIAL_NAME_MAP.put("47 Tucanae",             "杜鹃座47球状星团");
        CELESTIAL_NAME_MAP.put("Hercules Cluster",       "武仙座球状星团");
        CELESTIAL_NAME_MAP.put("Great Globular Cluster", "武仙座球状星团");

        // ---- 星座/天区 (Constellation) ----
        CELESTIAL_NAME_MAP.put("Orion",                  "猎户座");
        CELESTIAL_NAME_MAP.put("Scorpius",               "天蝎座");
        CELESTIAL_NAME_MAP.put("Cassiopeia",             "仙后座");
        CELESTIAL_NAME_MAP.put("Perseus",                "英仙座");
        CELESTIAL_NAME_MAP.put("Cygnus",                 "天鹅座");
        CELESTIAL_NAME_MAP.put("Sagittarius",            "射手座");
    }

    // =============================================
    // ⭐ v4.3 新增：天体类型分类表（用于前端 Tag 颜色区分）
    // =============================================
    private static final Map<String, String> CELESTIAL_TYPE_MAP = new HashMap<>();

    static {
        // 星云 - nebula
        for (String n : new String[]{
                "Orion Nebula","Crab Nebula","Eagle Nebula","Lagoon Nebula","Trifid Nebula",
                "Omega Nebula","Ring Nebula","Dumbbell Nebula","Helix Nebula","Horsehead Nebula",
                "Flame Nebula","California Nebula","Rosette Nebula","Veil Nebula","Bubble Nebula",
                "Wizard Nebula","Cone Nebula","North America Nebula","Eta Carinae Nebula",
                "Tarantula Nebula","Running Man Nebula","Witch Head Nebula","Cygnus Loop",
                "Pillars of Creation","Thor's Helmet","Pac-Man Nebula","Heart Nebula","Soul Nebula",
                "IC 1805","IC 1848","Cassiopeia A","Pelican Nebula","Cat's Eye Nebula"
        }) { CELESTIAL_TYPE_MAP.put(n, "nebula"); }

        // 星系 - galaxy
        for (String g : new String[]{
                "Andromeda Galaxy","Triangulum Galaxy","Whirlpool Galaxy","Sombrero Galaxy",
                "Black Eye Galaxy","Pinwheel Galaxy","Sunflower Galaxy","Bode's Galaxy",
                "Cigar Galaxy","Large Magellanic Cloud","Small Magellanic Cloud"
        }) { CELESTIAL_TYPE_MAP.put(g, "galaxy"); }

        // 星团 - cluster
        for (String c : new String[]{
                "Pleiades","Hyades","Beehive Cluster","Double Cluster","Jewel Box Cluster",
                "Wild Duck Cluster","Omega Centauri","47 Tucanae","Hercules Cluster","Great Globular Cluster"
        }) { CELESTIAL_TYPE_MAP.put(c, "cluster"); }

        // 星座 - constellation
        for (String c : new String[]{
                "Orion","Scorpius","Cassiopeia","Perseus","Cygnus","Sagittarius"
        }) { CELESTIAL_TYPE_MAP.put(c, "constellation"); }
    }


    // ============================================================
    // v4.1: submit
    // ============================================================

    @Override
    public RecognitionVO submit(SubmitRecognitionDTO dto, Long userId) {
        log.info("[Recognition] 用户 {} 提交星图识别", userId);

        // 1. 创建初始记录（status=0）
        Recognition recognition = new Recognition();
        recognition.setUserId(userId);
        recognition.setImageData(dto.getImageData());
        recognition.setStatus(0);
        recognitionMapper.insert(recognition);

        Long recognitionId = recognition.getId();
        log.info("[Recognition] 记录已创建, id={}", recognitionId);

        // 2. 触发异步提交（通过注入的 Bean 调用，保证 @Async 代理生效）
        try {
            astrometryService.submitAsync(recognitionId);
        } catch (Exception e) {
            log.error("[Recognition] 触发异步任务失败, id={}", recognitionId, e);
        }

        // 3. 立即返回
        RecognitionVO vo = new RecognitionVO();
        vo.setId(recognitionId);
        vo.setStatus(0);
        vo.setCreateTime(recognition.getCreateTime());
        return vo;
    }

    // ============================================================
    // v4.1: getStatus（等待页轮询）
    // ============================================================

    @Override
    public RecognitionVO getStatus(Long recognitionId, Long userId) {
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        checkOwnership(recognition, recognitionId, userId);
        return convertToVO(recognition);
    }

    // ============================================================
    // v4.2: getDetail（结果页基础版）
    // ============================================================

    @Override
    public RecognitionVO getDetail(Long recognitionId, Long userId) {
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        checkOwnership(recognition, recognitionId, userId);
        return convertToVO(recognition);
    }

    // ============================================================
    // v4.2 → v4.5 升级: getHistory（支持 status 筛选 + 附加历史列表字段）
    // ============================================================

    @Override
    public Map<String, Object> getHistory(Long userId, int pageNum, int pageSize, Integer status) {
        // 参数合法性修正
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 10;

        // 使用 MyBatis-Plus 分页，支持可选 status 筛选
        LambdaQueryWrapper<Recognition> wrapper = new LambdaQueryWrapper<Recognition>()
                .eq(Recognition::getUserId, userId)
                .eq(status != null, Recognition::getStatus, status)
                .orderByDesc(Recognition::getCreateTime);

        IPage<Recognition> page = recognitionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 转 VO，附加历史列表专用字段（hasImage / mainObjects）
        final int finalPageNum = pageNum;
        List<RecognitionVO> voList = page.getRecords().stream()
                .map(r -> {
                    RecognitionVO vo = convertToVO(r);
                    // ⭐ v4.5 附加字段: hasImage（是否有上传图片）
                    vo.setHasImage(r.getImageData() != null && !r.getImageData().isEmpty());
                    // ⭐ v4.5 附加字段: mainObjects（天体中文名列表）
                    if (vo.getObjectsInField() != null) {
                        List<String> mainObjects = vo.getObjectsInField().stream()
                                .map(en -> CELESTIAL_NAME_MAP.getOrDefault(en, en))
                                .collect(Collectors.toList());
                        vo.setMainObjects(mainObjects);
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", page.getTotal());
        result.put("pageNum", finalPageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    // ============================================================
    // ⭐ v4.3 新增: getResult（含中英文天体名称 + 坐标格式化）
    // ============================================================

    @Override
    public RecognitionVO getResult(Long recognitionId, Long userId) {
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        checkOwnership(recognition, recognitionId, userId);

        // 先用基础方法组装 VO（复用现有逻辑）
        RecognitionVO vo = convertToVO(recognition);

        // status != 1（识别未成功）：直接返回，不附加格式化字段
        if (recognition.getStatus() == null || recognition.getStatus() != 1) {
            return vo;
        }

        // ---- 识别成功：追加格式化字段 ----

        // 1. 天体中英文对照列表
        if (vo.getObjectsInField() != null && !vo.getObjectsInField().isEmpty()) {
            List<RecognitionVO.CelestialObjectVO> celestialObjects =
                    buildCelestialObjects(vo.getObjectsInField());
            vo.setCelestialObjects(celestialObjects);
        }

        // 2. 坐标格式化
        if (recognition.getRa() != null) {
            vo.setRaFormatted(formatRA(recognition.getRa().doubleValue()));
        }
        if (recognition.getDec() != null) {
            vo.setDecFormatted(formatDec(recognition.getDec().doubleValue()));
        }
        if (recognition.getOrientation() != null) {
            vo.setOrientationFormatted(
                    String.format("%.2f°", recognition.getOrientation().doubleValue()));
        }
        if (recognition.getRadius() != null) {
            vo.setRadiusFormatted(formatRadius(recognition.getRadius().doubleValue()));
        }

        return vo;
    }

    // ============================================================
    // ⭐ v4.5 新增: deleteRecord（删除单条记录）
    // ============================================================

    @Override
    public void deleteRecord(Long recognitionId, Long userId) {
        Recognition recognition = recognitionMapper.selectById(recognitionId);
        checkOwnership(recognition, recognitionId, userId);
        recognitionMapper.deleteById(recognitionId);
        log.info("[Recognition] 用户 {} 删除识别记录 {}", userId, recognitionId);
    }

    // ============================================================
    // ⭐ v4.5 新增: getStats（识别统计）
    // ============================================================

    @Override
    public RecognitionStatsVO getStats(Long userId) {
        // 只查 status 字段，避免加载 image_data 大字段
        LambdaQueryWrapper<Recognition> wrapper = new LambdaQueryWrapper<Recognition>()
                .eq(Recognition::getUserId, userId)
                .select(Recognition::getStatus);

        List<Recognition> all = recognitionMapper.selectList(wrapper);
        int total        = all.size();
        int successCount = (int) all.stream().filter(r -> Integer.valueOf(1).equals(r.getStatus())).count();
        int failCount    = (int) all.stream().filter(r -> Integer.valueOf(2).equals(r.getStatus())).count();
        int pendingCount = (int) all.stream().filter(r -> Integer.valueOf(0).equals(r.getStatus())).count();
        double successRate = total > 0 ? Math.round(successCount * 1000.0 / total) / 10.0 : 0.0;

        RecognitionStatsVO vo = new RecognitionStatsVO();
        vo.setTotal(total);
        vo.setSuccessCount(successCount);
        vo.setFailCount(failCount);
        vo.setPendingCount(pendingCount);
        vo.setSuccessRate(successRate);
        return vo;
    }

    // ============================================================
    // 私有工具方法
    // ============================================================

    /**
     * 鉴权检查：记录存在 且 属于当前用户
     */
    private void checkOwnership(Recognition recognition, Long recognitionId, Long userId) {
        if (recognition == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "识别记录不存在");
        }
        if (!recognition.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看此识别记录");
        }
    }

    /**
     * 实体转 VO（JSON 数组字段反序列化为 List）
     * 📌 原有方法保持不变
     */
    private RecognitionVO convertToVO(Recognition recognition) {
        RecognitionVO vo = new RecognitionVO();
        vo.setId(recognition.getId());
        vo.setStatus(recognition.getStatus());
        vo.setSubmissionId(recognition.getSubmissionId());
        vo.setJobId(recognition.getJobId());
        vo.setRa(recognition.getRa());
        vo.setDec(recognition.getDec());
        vo.setOrientation(recognition.getOrientation());
        vo.setRadius(recognition.getRadius());
        vo.setResultImageUrl(recognition.getResultImageUrl());
        vo.setFailReason(recognition.getFailReason());
        vo.setCreateTime(recognition.getCreateTime());

        // 解析 JSON 数组字段
        vo.setObjectsInField(parseJsonArray(recognition.getObjectsInField(), "objectsInField"));
        vo.setMachineTags(parseJsonArray(recognition.getMachineTags(), "machineTags"));

        if (recognition.getRecommendedProducts() != null) {
            try {
                List<Long> ids = JSON.parseArray(recognition.getRecommendedProducts(), Long.class);
                vo.setRecommendedProductIds(ids);
            } catch (Exception e) {
                log.warn("[Recognition] 解析 recommendedProducts 失败");
            }
        }

        return vo;
    }

    /** 安全解析 JSON 字符串数组 */
    private List<String> parseJsonArray(String jsonStr, String fieldName) {
        if (jsonStr == null) return null;
        try {
            return JSON.parseArray(jsonStr, String.class);
        } catch (Exception e) {
            log.warn("[Recognition] 解析 {} 失败: {}", fieldName, jsonStr);
            return null;
        }
    }

    // ============================================================
    // ⭐ v4.3 新增私有方法：天体名称映射 + 坐标格式化
    // ============================================================

    /**
     * 将英文天体名列表转换为中英文对照 VO 列表
     * 查 CELESTIAL_NAME_MAP，无匹配则 zh = en；查 CELESTIAL_TYPE_MAP，无匹配则 type = "unknown"
     */
    private List<RecognitionVO.CelestialObjectVO> buildCelestialObjects(List<String> englishNames) {
        return englishNames.stream().map(en -> {
            String zh   = CELESTIAL_NAME_MAP.getOrDefault(en, en);
            String type = CELESTIAL_TYPE_MAP.getOrDefault(en, "unknown");
            return new RecognitionVO.CelestialObjectVO(en, zh, type);
        }).collect(Collectors.toList());
    }

    /**
     * ⭐ 赤经(RA) 度数 → 时分秒格式化
     * 算法: 1小时 = 15度
     * 示例: 83.8221° → "05h 35m 17.3s"
     */
    private String formatRA(double raDegrees) {
        double raHours = raDegrees / 15.0;
        int    hours   = (int) raHours;
        double remMin  = (raHours - hours) * 60.0;
        int    minutes = (int) remMin;
        double seconds = (remMin - minutes) * 60.0;
        return String.format("%02dh %02dm %04.1fs", hours, minutes, seconds);
    }

    /**
     * ⭐ 赤纬(Dec) 度数 → 度分秒格式化
     * 示例: -5.3911° → "-05° 23' 28.0\""
     */
    private String formatDec(double decDegrees) {
        String sign   = decDegrees >= 0 ? "+" : "-";
        double abs    = Math.abs(decDegrees);
        int    deg    = (int) abs;
        double remArc = (abs - deg) * 60.0;
        int    arcMin = (int) remArc;
        double arcSec = (remArc - arcMin) * 60.0;
        return String.format("%s%02d° %02d' %04.1f\"", sign, deg, arcMin, arcSec);
    }

    /**
     * 视野半径格式化
     * ≥1° 显示度数；<1° 转换为角分
     */
    private String formatRadius(double radius) {
        if (radius >= 1.0) {
            return String.format("%.3f°", radius);
        } else {
            return String.format("%.1f'", radius * 60);
        }
    }
}
package com.astronomy.mall.module.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.astronomy.mall.module.user.service.UserOverviewService;
import com.astronomy.mall.module.user.vo.UserOverviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 个人中心概览 ServiceImpl
 *
 * 📌 聚合查询说明：
 *   1. 查 tb_user 获取基本信息 + 余额
 *   2. 查 tb_order 按状态 GROUP BY COUNT，得到各状态订单数
 *   3. 查 tb_refund 得到进行中退款数
 *   4. 查 tb_balance_log 取最新一条流水
 *   5. 查 tb_order 得到累计订单数 + 累计消费金额
 *   6. 查 tb_review 得到已发布评价数
 *   7. 组装 UserOverviewVO 返回
 *
 * 📌 待评价数说明：
 *   status=3(已完成) 且该订单下有至少一个商品未被当前用户评价
 *   查询略复杂，此处用近似方案：
 *     tb_order.status=3 的数量 - tb_review 中对应 order_id 数量
 *   若 order_item 里有多件商品只评了部分，实际应按 order_item 粒度统计，
 *   但概览页只需大致数字，此方案已足够。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOverviewServiceImpl implements UserOverviewService {

    private final UserMapper userMapper;

    /**
     * 注入自定义的概览 Mapper（多表聚合查询）
     * 📌 文件路径: com.astronomy.mall.module.user.mapper.UserOverviewMapper
     */
    @Resource
    private com.astronomy.mall.module.user.mapper.UserOverviewMapper userOverviewMapper;

    // 等级文字映射
    private static final String[] LEVEL_TEXT = {
            "", "入门观测者", "初级爱好者", "中级爱好者", "高级爱好者", "专家级玩家"
    };

    @Override
    public UserOverviewVO getOverview(Long userId) {

        // 1. 查用户基本信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserOverviewVO vo = new UserOverviewVO();

        // ── 用户基本信息 ──────────────────────────────────────
        vo.setUserId(user.getId());
        // nickname 为空时使用 username
        vo.setNickname(user.getNickname() != null && !user.getNickname().isEmpty()
                ? user.getNickname() : user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setCity(user.getCity());
        vo.setCreateTime(user.getCreateTime());

        // 观测等级
        int level = user.getObservationLevel() != null ? user.getObservationLevel() : 1;
        if (level < 1) level = 1;
        if (level > 5) level = 5;
        vo.setObservationLevel(level);
        vo.setObservationLevelText(LEVEL_TEXT[level]);

        // 解析兴趣标签 JSON 数组
        List<String> tags = new ArrayList<>();
        try {
            if (user.getInterestTags() != null && !user.getInterestTags().isEmpty()) {
                tags = JSON.parseObject(user.getInterestTags(), new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("[概览] 解析兴趣标签失败, userId={}, tags={}", userId, user.getInterestTags());
        }
        vo.setInterestTags(tags);

        // ── 余额 ─────────────────────────────────────────────
        vo.setBalance(user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);

        // 2. 查订单状态统计（调用 Mapper 聚合 SQL）
        try {
            Map<String, Object> orderStats = userOverviewMapper.selectOrderStatusCount(userId);
            if (orderStats != null) {
                vo.setPendingPayCount(toInt(orderStats.get("pendingPay")));
                vo.setPendingShipCount(toInt(orderStats.get("pendingShip")));
                vo.setPendingReceiveCount(toInt(orderStats.get("pendingReceive")));
                vo.setPendingReviewCount(toInt(orderStats.get("pendingReview")));
            } else {
                vo.setPendingPayCount(0);
                vo.setPendingShipCount(0);
                vo.setPendingReceiveCount(0);
                vo.setPendingReviewCount(0);
            }
        } catch (Exception e) {
            log.error("[概览] 查询订单状态统计失败, userId={}", userId, e);
            vo.setPendingPayCount(0);
            vo.setPendingShipCount(0);
            vo.setPendingReceiveCount(0);
            vo.setPendingReviewCount(0);
        }

        // 3. 查进行中退款数
        try {
            int refundingCount = userOverviewMapper.selectRefundingCount(userId);
            vo.setRefundingCount(refundingCount);
        } catch (Exception e) {
            log.error("[概览] 查询退款数失败, userId={}", userId, e);
            vo.setRefundingCount(0);
        }

        // 4. 查最近一条余额流水
        try {
            Map<String, Object> lastLog = userOverviewMapper.selectLastBalanceLog(userId);
            if (lastLog != null) {
                vo.setLastLogRemark(toString(lastLog.get("remark")));
                vo.setLastLogAmount(toBigDecimal(lastLog.get("amount")));
                vo.setLastLogTime(toLocalDateTime(lastLog.get("createTime")));
                vo.setLastLogType(toInt(lastLog.get("type")));
            }
        } catch (Exception e) {
            log.warn("[概览] 查询余额流水失败, userId={}", userId, e);
            // 流水查询失败不影响整体概览
        }

        // 5. 查累计订单总数
        try {
            vo.setTotalOrders(userOverviewMapper.selectTotalOrders(userId));
        } catch (Exception e) {
            log.warn("[概览] 查询累计订单数失败, userId={}", userId, e);
            vo.setTotalOrders(0);
        }

        // 6. 查累计消费金额（仅已完成订单）
        try {
            BigDecimal totalSpent = userOverviewMapper.selectTotalSpent(userId);
            vo.setTotalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO);
        } catch (Exception e) {
            log.warn("[概览] 查询累计消费失败, userId={}", userId, e);
            vo.setTotalSpent(BigDecimal.ZERO);
        }

        // 7. 查已发布评价数
        try {
            vo.setTotalReviews(userOverviewMapper.selectTotalReviews(userId));
        } catch (Exception e) {
            log.warn("[概览] 查询评价数失败, userId={}", userId, e);
            vo.setTotalReviews(0);
        }

        return vo;
    }

    // ── 类型转换工具方法 ─────────────────────────────────────────────

    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private java.time.LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) obj;
        // Timestamp → LocalDateTime
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        return null;
    }
}
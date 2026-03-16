package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.NotificationRecordQueryDTO;
import com.astronomy.mall.module.admin.mapper.AdminNotificationMapper;
import com.astronomy.mall.module.admin.service.AdminNotificationService;
import com.astronomy.mall.module.admin.vo.NotificationRecordVO;
import com.astronomy.mall.module.admin.vo.NotificationStatsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 后台通知记录管理 ServiceImpl
 *
 * 📌 依赖 AdminNotificationMapper（位于 admin.mapper 包）
 * 📌 删除操作使用逻辑删除，不物理删除，保留审计可追溯性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final AdminNotificationMapper adminNotificationMapper;

    // ==================== 模块中文标签映射 ====================
    private static final Map<String, String> MODULE_LABEL_MAP = new LinkedHashMap<>();
    // ==================== 通知类型中文标签映射 ====================
    private static final Map<String, String> TYPE_LABEL_MAP = new LinkedHashMap<>();

    static {
        // 模块标签
        MODULE_LABEL_MAP.put("mall",      "商城");
        MODULE_LABEL_MAP.put("system",    "系统");
        MODULE_LABEL_MAP.put("forum",     "论坛");
        MODULE_LABEL_MAP.put("course",    "课程");
        MODULE_LABEL_MAP.put("location",  "地理位置");
        MODULE_LABEL_MAP.put("recommend", "推荐系统");
        MODULE_LABEL_MAP.put("ai",        "AI识别");

        // 通知类型标签（商城）
        TYPE_LABEL_MAP.put("order_paid",               "订单支付成功");
        TYPE_LABEL_MAP.put("order_shipped",            "订单已发货");
        TYPE_LABEL_MAP.put("order_delivering",         "订单派送中");
        TYPE_LABEL_MAP.put("order_completed",          "订单已完成");
        TYPE_LABEL_MAP.put("order_cancelled",          "订单已取消");
        TYPE_LABEL_MAP.put("refund_approved",          "退款审核通过");
        TYPE_LABEL_MAP.put("refund_rejected",          "退款审核拒绝");
        TYPE_LABEL_MAP.put("refund_completed",         "退款已到账");
        TYPE_LABEL_MAP.put("product_on_sale",          "商品上架提醒");
        TYPE_LABEL_MAP.put("product_price_down",       "商品降价提醒");
        TYPE_LABEL_MAP.put("installation_confirmed",   "安装预约已确认");
        TYPE_LABEL_MAP.put("installation_cancelled",   "安装预约已取消");
        TYPE_LABEL_MAP.put("recycling_completed",      "二手回收款已到账");
        // 系统
        TYPE_LABEL_MAP.put("announcement",  "系统公告");
        TYPE_LABEL_MAP.put("security",      "账号安全");
        TYPE_LABEL_MAP.put("version_update","版本更新");
        TYPE_LABEL_MAP.put("promotion",     "活动推广");
    }

    // ==================== Service 方法 ====================

    @Override
    public IPage<NotificationRecordVO> getNotificationPage(NotificationRecordQueryDTO dto) {
        // 参数防御：pageSize 最大100条
        if (dto.getPageSize() > 100) {
            dto.setPageSize(100);
        }

        Page<NotificationRecordVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<NotificationRecordVO> result = adminNotificationMapper.selectNotificationPage(page, dto);

        // 填充中文标签
        result.getRecords().forEach(this::fillLabels);

        log.info("[后台通知记录] 查询完成: total={}, pages={}", result.getTotal(), result.getPages());
        return result;
    }

    @Override
    public NotificationStatsVO getNotificationStats() {
        NotificationStatsVO stats = new NotificationStatsVO();

        // 1. 汇总数量（一次查询）
        Map<String, Object> summary = adminNotificationMapper.selectCountSummary();
        stats.setTotalCount(toLong(summary.get("totalCount")));
        stats.setReadCount(toLong(summary.get("readCount")));
        stats.setUnreadCount(toLong(summary.get("unreadCount")));
        stats.setTodayCount(toLong(summary.get("todayCount")));
        stats.setMonthCount(toLong(summary.get("monthCount")));

        // 2. 模块分布（带中文标签 + 百分比计算）
        List<NotificationStatsVO.ModuleStatItem> moduleStats = adminNotificationMapper.selectModuleStats();
        long total = stats.getTotalCount() > 0 ? stats.getTotalCount() : 1;
        for (NotificationStatsVO.ModuleStatItem item : moduleStats) {
            item.setModuleLabel(MODULE_LABEL_MAP.getOrDefault(item.getModule(), item.getModule()));
            double pct = (double) item.getCount() / total * 100;
            item.setPercentage(Math.round(pct * 10.0) / 10.0); // 保留1位小数
        }
        stats.setModuleStats(moduleStats);

        // 3. 近30天每日量
        stats.setDateStats(adminNotificationMapper.selectDateStats());

        // 4. 类型 Top10（带中文标签）
        List<NotificationStatsVO.TypeStatItem> typeStats = adminNotificationMapper.selectTypeStats();
        typeStats.forEach(item ->
                item.setTypeLabel(TYPE_LABEL_MAP.getOrDefault(item.getType(), item.getType())));
        stats.setTypeStats(typeStats);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteNotifications(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(400, "请选择要删除的通知记录");
        }
        if (ids.size() > 500) {
            throw new BusinessException(400, "单次最多批量删除500条记录");
        }

        int rows = adminNotificationMapper.batchLogicDelete(ids);
        log.info("[后台通知记录] 批量删除完成: ids数量={}, 实际影响行数={}", ids.size(), rows);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 填充中文标签（moduleLabel / priorityLabel）
     */
    private void fillLabels(NotificationRecordVO vo) {
        // 模块标签
        vo.setModuleLabel(MODULE_LABEL_MAP.getOrDefault(vo.getModule(), vo.getModule()));

        // 优先级标签
        switch (vo.getPriority() == null ? 0 : vo.getPriority()) {
            case 1:
                vo.setPriorityLabel("重要");
                break;
            case 2:
                vo.setPriorityLabel("紧急");
                break;
            default:
                vo.setPriorityLabel("普通");
        }
    }

    /**
     * 安全转 Long（MyBatis selectMap 返回 BigDecimal 需要转换）
     */
    private Long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }
}
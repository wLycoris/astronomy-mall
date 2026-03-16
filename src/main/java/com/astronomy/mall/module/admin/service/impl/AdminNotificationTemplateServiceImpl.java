package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.TemplateStatusDTO;
import com.astronomy.mall.module.admin.dto.TemplateUpdateDTO;
import com.astronomy.mall.module.admin.service.AdminNotificationTemplateService;
import com.astronomy.mall.module.admin.vo.NotificationTemplateVO;
import com.astronomy.mall.module.notification.entity.NotificationTemplate;
import com.astronomy.mall.module.notification.mapper.NotificationTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台通知模板管理 ServiceImpl
 *
 * 📌 复用 notification 模块的 NotificationTemplateMapper（不新建Mapper）
 * 📌 "恢复默认" 功能通过 DEFAULT_TEMPLATES 静态Map实现
 *    默认内容与数据库 INSERT 初始化语句完全一致
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationTemplateServiceImpl implements AdminNotificationTemplateService {

    private final NotificationTemplateMapper notificationTemplateMapper;

    // ==================== 模块排序（决定分组展示顺序） ====================
    private static final List<String> MODULE_ORDER = Arrays.asList(
            "mall", "system", "forum", "course", "location", "recommend", "ai"
    );

    private static final Map<String, String> MODULE_LABEL_MAP = new LinkedHashMap<>();

    static {
        MODULE_LABEL_MAP.put("mall",      "商城模块");
        MODULE_LABEL_MAP.put("system",    "系统模块");
        MODULE_LABEL_MAP.put("forum",     "论坛模块");
        MODULE_LABEL_MAP.put("course",    "课程模块");
        MODULE_LABEL_MAP.put("location",  "地理位置模块");
        MODULE_LABEL_MAP.put("recommend", "推荐系统");
        MODULE_LABEL_MAP.put("ai",        "AI识别模块");
    }

    // ==================== 默认模板内容（与数据库初始化SQL完全一致） ====================
    /**
     * Key   = template.code
     * Value = [titleTemplate, contentTemplate, jumpUrlTemplate]
     */
    private static final Map<String, String[]> DEFAULT_TEMPLATES = new HashMap<>();

    static {
        // 商城模块
        DEFAULT_TEMPLATES.put("MALL_ORDER_PAID",
                new String[]{"订单支付成功", "您的订单{orderNo}已支付成功,金额¥{amount}", "/order/detail?id={orderId}"});
        DEFAULT_TEMPLATES.put("MALL_ORDER_SHIPPED",
                new String[]{"订单已发货", "您的订单{orderNo}已通过{logisticsCompany}发货,快递单号:{trackingNumber}", "/order/detail?id={orderId}"});
        DEFAULT_TEMPLATES.put("MALL_ORDER_DELIVERING",
                new String[]{"订单派送中", "您的包裹正在派送中,请保持手机畅通,快递单号:{trackingNumber}", "/order/detail?id={orderId}"});
        DEFAULT_TEMPLATES.put("MALL_ORDER_COMPLETED",
                new String[]{"订单已完成", "订单{orderNo}已完成,期待您的评价~", "/order/detail?id={orderId}"});
        DEFAULT_TEMPLATES.put("MALL_ORDER_CANCELLED",
                new String[]{"订单已取消", "订单{orderNo}已取消,退款将在1-3个工作日内原路返回", "/order/detail?id={orderId}"});
        DEFAULT_TEMPLATES.put("MALL_REFUND_APPROVED",
                new String[]{"退款审核通过", "您的退款申请已通过审核,退款金额¥{amount}将在1-3个工作日内到账", "/order/detail/{orderId}"});
        DEFAULT_TEMPLATES.put("MALL_REFUND_REJECTED",
                new String[]{"退款审核拒绝", "您的退款申请未通过审核,原因:{reason}", "/order/detail/{orderId}"});
        DEFAULT_TEMPLATES.put("MALL_REFUND_COMPLETED",
                new String[]{"退款已到账", "退款金额¥{amount}已到账,请查收", "/order/detail/{orderId}"});
        DEFAULT_TEMPLATES.put("MALL_PRODUCT_ON_SALE",
                new String[]{"商品上架提醒", "您关注的商品\"{productName}\"已上架", "/product/detail?id={productId}"});
        DEFAULT_TEMPLATES.put("MALL_PRODUCT_PRICE_DOWN",
                new String[]{"商品降价提醒", "您关注的商品\"{productName}\"降价啦!现价¥{price}", "/product/detail?id={productId}"});
        DEFAULT_TEMPLATES.put("MALL_INSTALLATION_CONFIRMED",
                new String[]{"安装预约已确认", "您的安装预约已确认，工程师{engineerName}将于{confirmedTime}上门，联系方式:{engineerPhone}", "/after-sale/installation"});
        DEFAULT_TEMPLATES.put("MALL_INSTALLATION_CANCELLED",
                new String[]{"安装预约已取消", "您的安装预约已被取消，原因：{reason}", "/after-sale/installation"});
        DEFAULT_TEMPLATES.put("MALL_RECYCLING_COMPLETED",
                new String[]{"二手回收款已到账", "您的回收申请{recycleNo}已完成，¥{amount}已到账至您的钱包，快去看看吧！", "/user/wallet"});
        // 系统模块
        DEFAULT_TEMPLATES.put("SYSTEM_ANNOUNCEMENT",
                new String[]{"系统公告", "{title}", "/notice/detail?id={noticeId}"});
        DEFAULT_TEMPLATES.put("SYSTEM_SECURITY",
                new String[]{"账号安全", "{message}", "/user/security"});
        DEFAULT_TEMPLATES.put("SYSTEM_VERSION_UPDATE",
                new String[]{"版本更新", "发现新版本v{version},{description}", null});
        DEFAULT_TEMPLATES.put("SYSTEM_PROMOTION",
                new String[]{"活动推广", "{title}", "/activity/detail?id={activityId}"});
    }

    // ==================== Service 方法 ====================

    @Override
    public Map<String, List<NotificationTemplateVO>> getTemplateListGrouped() {
        // 查询所有模板（不过滤 enabled，管理端需要看到全部）
        List<NotificationTemplate> templates = notificationTemplateMapper.selectList(null);

        // 转 VO
        List<NotificationTemplateVO> voList = templates.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        // 按模块分组（使用 LinkedHashMap 保持顺序）
        Map<String, List<NotificationTemplateVO>> grouped = new LinkedHashMap<>();

        // 按预定顺序填充分组
        for (String module : MODULE_ORDER) {
            String label = MODULE_LABEL_MAP.getOrDefault(module, module);
            List<NotificationTemplateVO> items = voList.stream()
                    .filter(v -> module.equals(v.getModule()))
                    .collect(Collectors.toList());
            if (!items.isEmpty()) {
                grouped.put(label, items);
            }
        }

        // 处理 MODULE_ORDER 中未定义的模块（兜底）
        for (NotificationTemplateVO vo : voList) {
            String label = MODULE_LABEL_MAP.getOrDefault(vo.getModule(), vo.getModule());
            if (!grouped.containsKey(label)) {
                grouped.put(label, new ArrayList<>());
            }
            if (!grouped.get(label).contains(vo)) {
                grouped.get(label).add(vo);
            }
        }

        log.info("[通知模板] 分组查询完成: 共{}个模板, {}个分组", voList.size(), grouped.size());
        return grouped;
    }

    @Override
    public NotificationTemplateVO getTemplateById(Long id) {
        NotificationTemplate template = notificationTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "通知模板不存在");
        }
        return toVO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(Long id, TemplateUpdateDTO dto) {
        NotificationTemplate template = notificationTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "通知模板不存在");
        }

        // 只允许修改内容相关字段，code/module/type 不允许改
        template.setTitleTemplate(dto.getTitleTemplate());
        template.setContentTemplate(dto.getContentTemplate());
        template.setJumpUrlTemplate(dto.getJumpUrlTemplate());
        template.setRemark(dto.getRemark());
        template.setUpdateTime(LocalDateTime.now());

        notificationTemplateMapper.updateById(template);
        log.info("[通知模板] 编辑成功: id={}, code={}", id, template.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplateStatus(TemplateStatusDTO dto) {
        NotificationTemplate template = notificationTemplateMapper.selectById(dto.getId());
        if (template == null) {
            throw new BusinessException(404, "通知模板不存在");
        }

        template.setEnabled(dto.getEnabled());
        template.setUpdateTime(LocalDateTime.now());
        notificationTemplateMapper.updateById(template);

        String action = dto.getEnabled() == 1 ? "启用" : "禁用";
        log.info("[通知模板] {}成功: id={}, code={}", action, dto.getId(), template.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetTemplate(Long id) {
        NotificationTemplate template = notificationTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(404, "通知模板不存在");
        }

        String[] defaults = DEFAULT_TEMPLATES.get(template.getCode());
        if (defaults == null) {
            throw new BusinessException(400, "该模板暂无默认内容配置，无法恢复");
        }

        // 恢复默认值
        template.setTitleTemplate(defaults[0]);
        template.setContentTemplate(defaults[1]);
        template.setJumpUrlTemplate(defaults[2]); // 可能为 null（如版本更新模板）
        template.setEnabled(1);  // 恢复默认同时启用
        template.setUpdateTime(LocalDateTime.now());

        notificationTemplateMapper.updateById(template);
        log.info("[通知模板] 恢复默认成功: id={}, code={}", id, template.getCode());
    }

    // ==================== 私有辅助方法 ====================

    /** Entity -> VO 转换 */
    private NotificationTemplateVO toVO(NotificationTemplate t) {
        NotificationTemplateVO vo = new NotificationTemplateVO();
        BeanUtils.copyProperties(t, vo);
        vo.setModuleLabel(MODULE_LABEL_MAP.getOrDefault(t.getModule(), t.getModule()));
        return vo;
    }
}
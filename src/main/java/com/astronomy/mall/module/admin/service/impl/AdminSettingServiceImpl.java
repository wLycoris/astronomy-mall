package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.entity.SystemSetting;
import com.astronomy.mall.module.admin.mapper.SystemSettingMapper;
import com.astronomy.mall.module.admin.service.AdminSettingService;
import com.astronomy.mall.module.admin.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置 ServiceImpl
 *
 * 📌 核心设计:
 *   1. 查询时: selectByGroupName 一次拉取整个分组 → 转成 Map<key, value> → 映射到 VO
 *   2. 更新时: VO 字段逐一调用 upsert(groupName, key, value) 写入数据库
 *      upsert 逻辑: 存在则 UPDATE，不存在则 INSERT（用 MyBatis-Plus updateById / save）
 *
 * 📌 字段命名映射说明 (Java camelCase ↔ DB snake_case):
 *   MyBatis-Plus 自动处理，实体类字段 settingKey 对应 DB 字段 setting_key
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSettingServiceImpl implements AdminSettingService {

    private final SystemSettingMapper systemSettingMapper;

    // ============================================================
    //  通用工具方法
    // ============================================================

    /**
     * 将 List<SystemSetting> 转换为 Map<settingKey, settingValue>
     * 方便后续用 key 快速取值
     */
    private Map<String, String> toMap(List<SystemSetting> list) {
        Map<String, String> map = new HashMap<>();
        for (SystemSetting s : list) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        return map;
    }

    /**
     * 从 Map 取字符串值，不存在返回 defaultVal
     */
    private String getString(Map<String, String> map, String key, String defaultVal) {
        String v = map.get(key);
        return (v != null && !v.isEmpty()) ? v : defaultVal;
    }

    /**
     * 从 Map 取 Boolean 值
     * "true" → true，其他 → false
     */
    private Boolean getBoolean(Map<String, String> map, String key, Boolean defaultVal) {
        String v = map.get(key);
        if (v == null) return defaultVal;
        return "true".equalsIgnoreCase(v.trim());
    }

    /**
     * 从 Map 取 Integer 值
     */
    private Integer getInteger(Map<String, String> map, String key, Integer defaultVal) {
        String v = map.get(key);
        if (v == null) return defaultVal;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 从 Map 取 BigDecimal 值
     */
    private BigDecimal getBigDecimal(Map<String, String> map, String key, BigDecimal defaultVal) {
        String v = map.get(key);
        if (v == null) return defaultVal;
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 更新或插入单条配置 (Upsert)
     *
     * 逻辑: 先查找是否存在，存在则 UPDATE，不存在则 INSERT
     * 使用 MyBatis-Plus 的 selectByGroupAndKey + updateById / save
     */
    private void upsert(String groupName, String key, String value) {
        SystemSetting existing = systemSettingMapper.selectByGroupAndKey(groupName, key);
        if (existing != null) {
            // 只更新 setting_value 字段
            existing.setSettingValue(value);
            systemSettingMapper.updateById(existing);
        } else {
            // 新增一条（理论上初始化 SQL 已插入，这里兜底）
            SystemSetting newSetting = new SystemSetting();
            newSetting.setGroupName(groupName);
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            systemSettingMapper.insert(newSetting);
        }
    }

    /**
     * Boolean 转字符串存储
     */
    private String boolStr(Boolean b) {
        return (b != null && b) ? "true" : "false";
    }

    // ============================================================
    //  基础设置
    // ============================================================

    @Override
    public BasicSettingVO getBasicSetting() {
        List<SystemSetting> list = systemSettingMapper.selectByGroupName("basic");
        Map<String, String> map = toMap(list);

        BasicSettingVO vo = new BasicSettingVO();
        vo.setMallName(getString(map, "mall_name", "天文器材商城"));
        vo.setMallLogo(getString(map, "mall_logo", ""));
        vo.setMallDesc(getString(map, "mall_desc", ""));
        vo.setContactPhone(getString(map, "contact_phone", ""));
        vo.setContactEmail(getString(map, "contact_email", ""));
        vo.setContactQq(getString(map, "contact_qq", ""));
        vo.setIcpNumber(getString(map, "icp_number", ""));
        vo.setCopyright(getString(map, "copyright", ""));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBasicSetting(BasicSettingDTO dto) {
        upsert("basic", "mall_name",    dto.getMallName() != null ? dto.getMallName() : "");
        upsert("basic", "mall_logo",    dto.getMallLogo() != null ? dto.getMallLogo() : "");
        upsert("basic", "mall_desc",    dto.getMallDesc() != null ? dto.getMallDesc() : "");
        upsert("basic", "contact_phone",dto.getContactPhone() != null ? dto.getContactPhone() : "");
        upsert("basic", "contact_email",dto.getContactEmail() != null ? dto.getContactEmail() : "");
        upsert("basic", "contact_qq",   dto.getContactQq() != null ? dto.getContactQq() : "");
        upsert("basic", "icp_number",   dto.getIcpNumber() != null ? dto.getIcpNumber() : "");
        upsert("basic", "copyright",    dto.getCopyright() != null ? dto.getCopyright() : "");
        log.info("[系统设置] 基础设置已更新");
    }

    // ============================================================
    //  运费设置
    // ============================================================

    @Override
    public FreightSettingVO getFreightSetting() {
        List<SystemSetting> list = systemSettingMapper.selectByGroupName("freight");
        Map<String, String> map = toMap(list);

        FreightSettingVO vo = new FreightSettingVO();
        vo.setDefaultFreight(getBigDecimal(map, "default_freight", BigDecimal.TEN));
        vo.setFreeFreightEnabled(getBoolean(map, "free_freight_enabled", true));
        vo.setFreeFreightAmount(getBigDecimal(map, "free_freight_amount", new BigDecimal("99")));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFreightSetting(FreightSettingDTO dto) {
        upsert("freight", "default_freight",    dto.getDefaultFreight().toPlainString());
        upsert("freight", "free_freight_enabled", boolStr(dto.getFreeFreightEnabled()));
        BigDecimal freeAmount = dto.getFreeFreightAmount() != null ? dto.getFreeFreightAmount() : BigDecimal.ZERO;
        upsert("freight", "free_freight_amount", freeAmount.toPlainString());
        log.info("[系统设置] 运费设置已更新");
    }

    // ============================================================
    //  支付设置
    // ============================================================

    @Override
    public PaymentSettingVO getPaymentSetting() {
        List<SystemSetting> list = systemSettingMapper.selectByGroupName("payment");
        Map<String, String> map = toMap(list);

        PaymentSettingVO vo = new PaymentSettingVO();
        vo.setAlipayEnabled(getBoolean(map, "alipay_enabled", true));
        vo.setWechatEnabled(getBoolean(map, "wechat_enabled", true));
        vo.setBalanceEnabled(getBoolean(map, "balance_enabled", true));
        vo.setPayTimeoutMinutes(getInteger(map, "pay_timeout_minutes", 15));
        vo.setAutoConfirmDays(getInteger(map, "auto_confirm_days", 7));
        vo.setAutoCloseDays(getInteger(map, "auto_close_days", 3));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentSetting(PaymentSettingDTO dto) {
        // 至少保留一种支付方式开启
        boolean anyEnabled = Boolean.TRUE.equals(dto.getAlipayEnabled())
                || Boolean.TRUE.equals(dto.getWechatEnabled())
                || Boolean.TRUE.equals(dto.getBalanceEnabled());
        if (!anyEnabled) {
            throw new com.astronomy.mall.common.exception.BusinessException("至少需要开启一种支付方式");
        }

        upsert("payment", "alipay_enabled",      boolStr(dto.getAlipayEnabled()));
        upsert("payment", "wechat_enabled",       boolStr(dto.getWechatEnabled()));
        upsert("payment", "balance_enabled",      boolStr(dto.getBalanceEnabled()));
        upsert("payment", "pay_timeout_minutes",  String.valueOf(dto.getPayTimeoutMinutes()));
        upsert("payment", "auto_confirm_days",    String.valueOf(dto.getAutoConfirmDays()));
        upsert("payment", "auto_close_days",      String.valueOf(dto.getAutoCloseDays()));
        log.info("[系统设置] 支付设置已更新");
    }

    // ============================================================
    //  SEO 设置
    // ============================================================

    @Override
    public SeoSettingVO getSeoSetting() {
        List<SystemSetting> list = systemSettingMapper.selectByGroupName("seo");
        Map<String, String> map = toMap(list);

        SeoSettingVO vo = new SeoSettingVO();
        vo.setSeoTitle(getString(map, "seo_title", ""));
        vo.setSeoKeywords(getString(map, "seo_keywords", ""));
        vo.setSeoDescription(getString(map, "seo_description", ""));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeoSetting(SeoSettingDTO dto) {
        upsert("seo", "seo_title",       dto.getSeoTitle() != null ? dto.getSeoTitle() : "");
        upsert("seo", "seo_keywords",    dto.getSeoKeywords() != null ? dto.getSeoKeywords() : "");
        upsert("seo", "seo_description", dto.getSeoDescription() != null ? dto.getSeoDescription() : "");
        log.info("[系统设置] SEO 设置已更新");
    }

    // ============================================================
    //  注册设置
    // ============================================================

    @Override
    public RegisterSettingVO getRegisterSetting() {
        List<SystemSetting> list = systemSettingMapper.selectByGroupName("register");
        Map<String, String> map = toMap(list);

        RegisterSettingVO vo = new RegisterSettingVO();
        vo.setRegisterEnabled(getBoolean(map, "register_enabled", true));
        vo.setEmailVerifyEnabled(getBoolean(map, "email_verify_enabled", false));
        vo.setInviteOnly(getBoolean(map, "invite_only", false));
        vo.setDefaultAvatar(getString(map, "default_avatar",
                "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRegisterSetting(RegisterSettingDTO dto) {
        upsert("register", "register_enabled",      boolStr(dto.getRegisterEnabled()));
        upsert("register", "email_verify_enabled",  boolStr(dto.getEmailVerifyEnabled()));
        upsert("register", "invite_only",           boolStr(dto.getInviteOnly()));
        upsert("register", "default_avatar",
                dto.getDefaultAvatar() != null ? dto.getDefaultAvatar() : "");
        log.info("[系统设置] 注册设置已更新");
    }

    // ============================================================
    //  维护模式
    // ============================================================

    @Override
    public MaintenanceSettingVO getMaintenanceSetting() {
        List<SystemSetting> list = systemSettingMapper.selectByGroupName("maintenance");
        Map<String, String> map = toMap(list);

        MaintenanceSettingVO vo = new MaintenanceSettingVO();
        vo.setMaintenanceMode(getBoolean(map, "maintenance_mode", false));
        vo.setMaintenanceMessage(getString(map, "maintenance_message", "系统维护中，请稍后再访问..."));
        vo.setMaintenanceEndTime(getString(map, "maintenance_end_time", ""));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaintenanceSetting(MaintenanceSettingDTO dto) {
        upsert("maintenance", "maintenance_mode",    boolStr(dto.getMaintenanceMode()));
        upsert("maintenance", "maintenance_message",
                dto.getMaintenanceMessage() != null ? dto.getMaintenanceMessage() : "");
        upsert("maintenance", "maintenance_end_time",
                dto.getMaintenanceEndTime() != null ? dto.getMaintenanceEndTime() : "");
        log.info("[系统设置] 维护模式已{}",
                Boolean.TRUE.equals(dto.getMaintenanceMode()) ? "开启" : "关闭");
    }
}
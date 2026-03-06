package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.vo.*;

/**
 * 系统设置 Service 接口
 *
 * 📌 说明:
 *   每个配置分组对应一组 get/update 方法
 *   内部统一调用 settingHelper 进行 key-value 的读写
 *
 * 📌 分组:
 *   basic       - 基础设置
 *   freight     - 运费设置
 *   payment     - 支付设置
 *   seo         - SEO 设置
 *   register    - 注册设置
 *   maintenance - 维护模式
 */
public interface AdminSettingService {

    // ==================== 基础设置 ====================

    /**
     * 获取基础设置
     */
    BasicSettingVO getBasicSetting();

    /**
     * 更新基础设置
     *
     * @param dto 基础设置 DTO
     */
    void updateBasicSetting(BasicSettingDTO dto);

    // ==================== 运费设置 ====================

    /**
     * 获取运费设置
     */
    FreightSettingVO getFreightSetting();

    /**
     * 更新运费设置
     *
     * @param dto 运费设置 DTO
     */
    void updateFreightSetting(FreightSettingDTO dto);

    // ==================== 支付设置 ====================

    /**
     * 获取支付设置
     */
    PaymentSettingVO getPaymentSetting();

    /**
     * 更新支付设置
     *
     * @param dto 支付设置 DTO
     */
    void updatePaymentSetting(PaymentSettingDTO dto);

    // ==================== SEO 设置 ====================

    /**
     * 获取 SEO 设置
     */
    SeoSettingVO getSeoSetting();

    /**
     * 更新 SEO 设置
     *
     * @param dto SEO 设置 DTO
     */
    void updateSeoSetting(SeoSettingDTO dto);

    // ==================== 注册设置 ====================

    /**
     * 获取注册设置
     */
    RegisterSettingVO getRegisterSetting();

    /**
     * 更新注册设置
     *
     * @param dto 注册设置 DTO
     */
    void updateRegisterSetting(RegisterSettingDTO dto);

    // ==================== 维护模式 ====================

    /**
     * 获取维护模式设置
     */
    MaintenanceSettingVO getMaintenanceSetting();

    /**
     * 更新维护模式设置
     *
     * @param dto 维护模式 DTO
     */
    void updateMaintenanceSetting(MaintenanceSettingDTO dto);
}
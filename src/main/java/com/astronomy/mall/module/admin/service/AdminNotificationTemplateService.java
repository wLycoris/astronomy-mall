package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.TemplateStatusDTO;
import com.astronomy.mall.module.admin.dto.TemplateUpdateDTO;
import com.astronomy.mall.module.admin.vo.NotificationTemplateVO;

import java.util.List;
import java.util.Map;

/**
 * 后台通知模板管理 Service 接口
 *
 * 📌 职责：
 *   - 查询模板列表（按模块分组）
 *   - 查询模板详情
 *   - 编辑模板内容（标题/内容/跳转链接）
 *   - 启用/禁用模板
 *   - 恢复模板默认内容
 */
public interface AdminNotificationTemplateService {

    /**
     * 模板列表（按模块分组）
     * 返回 Map<moduleLabel, List<VO>>，前端直接渲染分组
     *
     * @return 按模块分组的模板列表
     */
    Map<String, List<NotificationTemplateVO>> getTemplateListGrouped();

    /**
     * 模板详情（编辑前回显数据）
     *
     * @param id 模板ID
     * @return 模板VO
     */
    NotificationTemplateVO getTemplateById(Long id);

    /**
     * 编辑模板（只允许修改标题/内容/跳转链接/备注）
     *
     * @param id  模板ID
     * @param dto 编辑数据
     */
    void updateTemplate(Long id, TemplateUpdateDTO dto);

    /**
     * 启用/禁用模板
     *
     * @param dto 包含 id 和目标 enabled 状态
     */
    void updateTemplateStatus(TemplateStatusDTO dto);

    /**
     * 恢复模板默认内容
     * 将模板内容恢复到系统内置的默认值
     *
     * @param id 模板ID
     */
    void resetTemplate(Long id);
}
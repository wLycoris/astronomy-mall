package com.astronomy.mall.module.admin.controller;

import com.astronomy.mall.common.annotation.AdminLog;
import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.admin.dto.TemplateStatusDTO;
import com.astronomy.mall.module.admin.dto.TemplateUpdateDTO;
import com.astronomy.mall.module.admin.service.AdminNotificationTemplateService;
import com.astronomy.mall.module.admin.vo.NotificationTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台通知模板管理 Controller
 *
 * 接口列表 (5个):
 *   GET  /api/admin/notification/template/list      - 模板列表（按模块分组）
 *   GET  /api/admin/notification/template/{id}      - 模板详情
 *   PUT  /api/admin/notification/template/{id}      - 编辑模板
 *   POST /api/admin/notification/template/status    - 启用/禁用模板
 *   POST /api/admin/notification/template/reset/{id} - 恢复默认内容
 *
 * 📌 所有接口需要管理员权限（AdminInterceptor 统一拦截 /api/admin/**）
 */
@Api(tags = "后台-通知模板管理")
@RestController
@RequestMapping("/api/admin/notification/template")
@RequiredArgsConstructor
public class AdminNotificationTemplateController {

    private final AdminNotificationTemplateService templateService;

    /**
     * 1. 模板列表（按模块分组展示）
     *
     * GET /api/admin/notification/template/list
     *
     * 返回结构: Map<模块中文名, List<模板VO>>
     * 例如:
     *   {
     *     "商城模块": [{ id:1, code:"MALL_ORDER_PAID", ... }, ...],
     *     "系统模块": [{ id:11, code:"SYSTEM_ANNOUNCEMENT", ... }, ...]
     *   }
     *
     * 📌 前端根据此结构渲染分组 el-tabs 或 el-collapse
     */
    @ApiOperation("模板列表（按模块分组）")
    @GetMapping("/list")
    public Result<Map<String, List<NotificationTemplateVO>>> getTemplateList() {
        Map<String, List<NotificationTemplateVO>> grouped = templateService.getTemplateListGrouped();
        return Result.success(grouped);
    }

    /**
     * 2. 模板详情
     *
     * GET /api/admin/notification/template/{id}
     *
     * 📌 用于编辑对话框打开前的数据回显
     */
    @ApiOperation("模板详情")
    @GetMapping("/{id}")
    public Result<NotificationTemplateVO> getTemplateDetail(@PathVariable Long id) {
        NotificationTemplateVO vo = templateService.getTemplateById(id);
        return Result.success(vo);
    }

    /**
     * 3. 编辑模板
     *
     * PUT /api/admin/notification/template/{id}
     *
     * 允许修改: titleTemplate / contentTemplate / jumpUrlTemplate / remark
     * 禁止修改: code / module / type（模板唯一标识，不允许更改）
     *
     * 📌 前端编辑对话框提交时调用
     */
    @ApiOperation("编辑模板")
    @AdminLog("编辑通知模板")
    @PutMapping("/{id}")
    public Result<Void> updateTemplate(
            @PathVariable Long id,
            @Validated @RequestBody TemplateUpdateDTO dto) {
        templateService.updateTemplate(id, dto);
        return Result.success(null);
    }

    /**
     * 4. 启用/禁用模板
     *
     * POST /api/admin/notification/template/status
     * Body: { "id": 1, "enabled": 0 }
     *
     * 📌 禁用后，NotificationService.sendNotification() 中若找不到启用的模板则跳过发送
     * 📌 enabled: 0-禁用, 1-启用
     */
    @ApiOperation("启用/禁用模板")
    @AdminLog("修改通知模板状态")
    @PostMapping("/status")
    public Result<Void> updateTemplateStatus(@Validated @RequestBody TemplateStatusDTO dto) {
        templateService.updateTemplateStatus(dto);
        return Result.success(null);
    }

    /**
     * 5. 恢复模板默认内容
     *
     * POST /api/admin/notification/template/reset/{id}
     *
     * 📌 将模板的 titleTemplate/contentTemplate/jumpUrlTemplate 恢复为系统内置默认值
     * 📌 同时将 enabled 恢复为 1（启用）
     * 📌 仅恢复内容，不影响 code/module/type 等标识字段
     */
    @ApiOperation("恢复模板默认内容")
    @AdminLog("恢复通知模板默认内容")
    @PostMapping("/reset/{id}")
    public Result<Void> resetTemplate(@PathVariable Long id) {
        templateService.resetTemplate(id);
        return Result.success(null);
    }
}
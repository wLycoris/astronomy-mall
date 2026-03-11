package com.astronomy.mall.module.aftersale.controller;

import com.astronomy.mall.common.result.Result;
import com.astronomy.mall.module.aftersale.dto.ServiceReminderDTO;
import com.astronomy.mall.module.aftersale.service.ServiceReminderService;
import com.astronomy.mall.module.aftersale.vo.ServiceReminderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 器材保养提醒 Controller
 *
 * 📌 接口列表（共4个，仅用户端）:
 *   GET    /api/service-reminder/list        - 我的提醒列表
 *   POST   /api/service-reminder/add         - 新增
 *   PUT    /api/service-reminder/update/{id} - 编辑（含标记完成）
 *   DELETE /api/service-reminder/delete/{id} - 删除
 *
 * ⚠️ 无管理员端接口（管理员不需要管用户的个人保养计划）
 *
 * 📌 认证方式:
 *   所有接口均需登录，userId 从 JwtInterceptor 存入的 request.getAttribute("userId") 获取
 *   接口前缀不带 /admin，不会触发 AdminInterceptor
 *
 * 路径: com.astronomy.mall.module.aftersale.controller.ServiceReminderController
 */
@RestController
@RequestMapping("/api/service-reminder")
@RequiredArgsConstructor
public class ServiceReminderController {

    private final ServiceReminderService serviceReminderService;

    // ===================== 工具方法 =====================

    /**
     * 从 request 中获取当前登录用户ID
     * JwtInterceptor 在验证 Token 后已将 userId 存入 request attribute
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    // ===================== 接口 =====================

    /**
     * 获取我的保养提醒列表
     * GET /api/service-reminder/list
     *
     * 📌 排序: 未完成优先 → 到期日期升序
     * 📌 前端根据 remindDate 计算到期颜色，后端只负责返回数据
     */
    @GetMapping("/list")
    public Result<List<ServiceReminderVO>> getMyList(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ServiceReminderVO> list = serviceReminderService.getMyList(userId);
        return Result.success(list);
    }

    /**
     * 新增保养提醒
     * POST /api/service-reminder/add
     *
     * 📌 必传字段: productName / remindTitle / remindDate
     * 📌 remindType 默认 "custom"（可选：clean/calibrate/check/custom）
     */
    @PostMapping("/add")
    public Result<Void> addReminder(
            @Validated @RequestBody ServiceReminderDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        serviceReminderService.addReminder(userId, dto);
        // ⚠️ 修复: Result.success("字符串") 返回 Result<String>，与 Result<Void> 不兼容
        // 改用 Result.success(null) 保持泛型一致
        return Result.success(null);
    }

    /**
     * 编辑保养提醒（含"标记已完成"/"续期设置下次提醒"）
     * PUT /api/service-reminder/update/{id}
     *
     * 📌 "标记已完成"使用此接口：传 isDone=1 即可
     * 📌 "完成后设置下次提醒"：传 isDone=1 + 新的 remindDate（系统自动重置完成状态，展示新一轮提醒）
     *
     * ⚠️ 前端使用此接口需注意：
     *   - 普通编辑: 传 productName / remindTitle / remindDate / remindType，不传 isDone
     *   - 标记完成: 传 isDone=1，可选传 remindDate（下次提醒日期）
     *   - 若用户"标记完成"并设置下次提醒，前端应提示"已完成！下次提醒已设置为 xx月xx日"
     */
    @PutMapping("/update/{id}")
    public Result<Void> updateReminder(
            @PathVariable Long id,
            @RequestBody ServiceReminderDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        serviceReminderService.updateReminder(userId, id, dto);
        return Result.success(null);
    }

    /**
     * 删除保养提醒（物理删除）
     * DELETE /api/service-reminder/delete/{id}
     *
     * ⚠️ 只能删除自己的提醒，后端会校验 userId
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteReminder(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        serviceReminderService.deleteReminder(userId, id);
        return Result.success(null);
    }
}
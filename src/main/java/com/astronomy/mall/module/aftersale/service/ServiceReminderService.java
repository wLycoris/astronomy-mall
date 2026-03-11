package com.astronomy.mall.module.aftersale.service;

import com.astronomy.mall.module.aftersale.dto.ServiceReminderDTO;
import com.astronomy.mall.module.aftersale.vo.ServiceReminderVO;

import java.util.List;

/**
 * 器材保养提醒 Service 接口
 *
 * 📌 业务规则:
 *   1. 所有接口均只操作当前登录用户自己的数据（userId 从 JWT 解析）
 *   2. 列表按 "未完成优先 → 到期日期升序" 排序，便于用户一眼看到紧急任务
 *   3. 无管理员端接口，管理员不需要管用户的个人保养计划
 *
 * 路径: com.astronomy.mall.module.aftersale.service.ServiceReminderService
 */
public interface ServiceReminderService {

    /**
     * 获取我的保养提醒列表
     * 排序规则: is_done ASC（未完成优先）, remind_date ASC（到期日期升序）
     *
     * @param userId 当前登录用户ID
     * @return 保养提醒VO列表
     */
    List<ServiceReminderVO> getMyList(Long userId);

    /**
     * 新增保养提醒
     *
     * @param userId 当前登录用户ID
     * @param dto    新增数据
     */
    void addReminder(Long userId, ServiceReminderDTO dto);

    /**
     * 编辑保养提醒（含标记完成/重新激活）
     *
     * 📌 "标记已完成"也走此接口：
     *   - 传 isDone=1 → 记录 doneTime = now()
     *   - 可同时更新 remindDate（前端选择"下次提醒日期"的场景）
     *   - 若 isDone=0 → 清除 doneTime（重新激活）
     *
     * @param userId 当前登录用户ID（用于权限校验，不允许修改他人数据）
     * @param id     提醒ID
     * @param dto    更新数据
     */
    void updateReminder(Long userId, Long id, ServiceReminderDTO dto);

    /**
     * 删除保养提醒（物理删除）
     *
     * @param userId 当前登录用户ID（权限校验）
     * @param id     提醒ID
     */
    void deleteReminder(Long userId, Long id);
}
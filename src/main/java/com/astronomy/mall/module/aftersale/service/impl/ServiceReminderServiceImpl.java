package com.astronomy.mall.module.aftersale.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.aftersale.dto.ServiceReminderDTO;
import com.astronomy.mall.module.aftersale.entity.ServiceReminder;
import com.astronomy.mall.module.aftersale.mapper.ServiceReminderMapper;
import com.astronomy.mall.module.aftersale.service.ServiceReminderService;
import com.astronomy.mall.module.aftersale.vo.ServiceReminderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 器材保养提醒 ServiceImpl
 *
 * 📌 所有方法均强制校验 userId，确保用户只能操作自己的数据
 * 📌 列表排序: 未完成 → 到期日期升序（MyBatis-Plus 链式排序）
 *
 * 路径: com.astronomy.mall.module.aftersale.service.impl.ServiceReminderServiceImpl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceReminderServiceImpl implements ServiceReminderService {

    private final ServiceReminderMapper serviceReminderMapper;

    // ===================== 保养类型中文映射 =====================

    /**
     * 将 remind_type 英文值转为中文标签
     * clean=光学清洁 / calibrate=赤道仪校准 / check=常规检查 / custom=自定义
     */
    private String convertTypeLabel(String remindType) {
        if (remindType == null) return "自定义";
        switch (remindType) {
            case "clean":     return "光学清洁";
            case "calibrate": return "赤道仪校准";
            case "check":     return "常规检查";
            case "custom":    return "自定义";
            default:          return remindType;
        }
    }

    // ===================== Entity → VO 转换 =====================

    /**
     * ServiceReminder → ServiceReminderVO
     */
    private ServiceReminderVO toVO(ServiceReminder entity) {
        ServiceReminderVO vo = new ServiceReminderVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setProductName(entity.getProductName());
        vo.setRemindType(entity.getRemindType());
        vo.setRemindTypeLabel(convertTypeLabel(entity.getRemindType()));
        vo.setRemindTitle(entity.getRemindTitle());
        vo.setRemindDate(entity.getRemindDate());
        vo.setIsDone(entity.getIsDone());
        vo.setDoneTime(entity.getDoneTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    // ===================== 权限校验 =====================

    /**
     * 查询提醒记录并校验归属用户
     *
     * @param id     提醒ID
     * @param userId 当前用户ID
     * @return 验证通过的实体
     * @throws BusinessException 记录不存在或不属于当前用户
     */
    private ServiceReminder getAndValidate(Long id, Long userId) {
        ServiceReminder reminder = serviceReminderMapper.selectById(id);
        if (reminder == null) {
            // ⚠️ 修复: BusinessException 接收 (Integer, String)，需用 .getCode() 取整数码
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "保养提醒不存在");
        }
        if (!reminder.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此保养提醒");
        }
        return reminder;
    }

    // ===================== 接口实现 =====================

    /**
     * 获取我的保养提醒列表
     * 排序: 未完成(is_done=0)优先，然后按 remind_date 升序
     */
    @Override
    public List<ServiceReminderVO> getMyList(Long userId) {
        LambdaQueryWrapper<ServiceReminder> wrapper = new LambdaQueryWrapper<ServiceReminder>()
                .eq(ServiceReminder::getUserId, userId)
                .orderByAsc(ServiceReminder::getIsDone)       // 未完成(0)排前面
                .orderByAsc(ServiceReminder::getRemindDate);  // 到期日期升序

        List<ServiceReminder> list = serviceReminderMapper.selectList(wrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 新增保养提醒
     */
    @Override
    public void addReminder(Long userId, ServiceReminderDTO dto) {
        ServiceReminder reminder = new ServiceReminder();
        reminder.setUserId(userId);
        reminder.setProductName(dto.getProductName());
        // 未传类型时默认 custom
        reminder.setRemindType(dto.getRemindType() != null ? dto.getRemindType() : "custom");
        reminder.setRemindTitle(dto.getRemindTitle());
        reminder.setRemindDate(dto.getRemindDate());
        reminder.setIsDone(0); // 新增默认未完成

        serviceReminderMapper.insert(reminder);
        log.info("[ServiceReminder] 用户 {} 新增保养提醒: {}", userId, dto.getRemindTitle());
    }

    /**
     * 编辑保养提醒（含"标记已完成"/"重新激活"/"修改下次提醒日期"）
     *
     * 📌 标记完成逻辑:
     *   - isDone=1 → 记录 doneTime=now()，并可同时更新 remindDate（下次提醒日期）
     *   - isDone=0 → 清除 doneTime（前端"重新激活"场景）
     *   - isDone=null → 不修改完成状态，仅更新其他字段
     */
    @Override
    public void updateReminder(Long userId, Long id, ServiceReminderDTO dto) {
        // 权限校验：只能修改自己的数据
        ServiceReminder reminder = getAndValidate(id, userId);

        // 更新基本信息（非空才覆盖，支持部分更新）
        if (dto.getProductName() != null) {
            reminder.setProductName(dto.getProductName());
        }
        if (dto.getRemindType() != null) {
            reminder.setRemindType(dto.getRemindType());
        }
        if (dto.getRemindTitle() != null) {
            reminder.setRemindTitle(dto.getRemindTitle());
        }
        if (dto.getRemindDate() != null) {
            // 标记完成时传的是"下次提醒日期"，或者普通编辑修改日期
            reminder.setRemindDate(dto.getRemindDate());
        }

        // 处理完成状态
        if (dto.getIsDone() != null) {
            reminder.setIsDone(dto.getIsDone());
            if (dto.getIsDone() == 1) {
                // 标记完成：记录完成时间
                reminder.setDoneTime(LocalDateTime.now());
                log.info("[ServiceReminder] 用户 {} 标记完成提醒 id={}", userId, id);
            } else {
                // 重新激活：清除完成时间
                reminder.setDoneTime(null);
                log.info("[ServiceReminder] 用户 {} 重新激活提醒 id={}", userId, id);
            }
        }

        serviceReminderMapper.updateById(reminder);
    }

    /**
     * 删除保养提醒（物理删除）
     */
    @Override
    public void deleteReminder(Long userId, Long id) {
        // 权限校验：只能删除自己的数据
        getAndValidate(id, userId);
        serviceReminderMapper.deleteById(id);
        log.info("[ServiceReminder] 用户 {} 删除保养提醒 id={}", userId, id);
    }
}
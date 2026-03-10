package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.admin.dto.InstallationAdminCancelDTO;
import com.astronomy.mall.module.admin.dto.InstallationConfirmDTO;
import com.astronomy.mall.module.admin.dto.InstallationQueryDTO;
import com.astronomy.mall.module.admin.service.AdminInstallationService;
import com.astronomy.mall.module.admin.vo.AdminInstallationVO;
import com.astronomy.mall.module.aftersale.entity.Installation;
import com.astronomy.mall.module.aftersale.mapper.InstallationMapper;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * 管理员安装预约 ServiceImpl
 *
 * 📌 文件路径:
 *   module/admin/service/impl/AdminInstallationServiceImpl.java
 *
 * 📌 通知集成:
 *   confirmInstallation() 成功后调用
 *   NotificationHelper.sendInstallationConfirmedNotification()
 *   发送 MALL_INSTALLATION_CONFIRMED 类型通知
 *
 * ⚠️ 需要在 NotificationHelper.java 中添加
 *    sendInstallationConfirmedNotification() 方法
 *    (见 notification/NotificationHelper新增方法说明.md)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminInstallationServiceImpl implements AdminInstallationService {

    private final InstallationMapper installationMapper;
    private final NotificationHelper notificationHelper;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ─────────────────────────────────────────────
    // 查询预约列表
    // ─────────────────────────────────────────────

    @Override
    public IPage<AdminInstallationVO> getList(InstallationQueryDTO dto) {
        IPage<AdminInstallationVO> page = new Page<>(
                dto.getPageNum() == null ? 1 : dto.getPageNum(),
                dto.getPageSize() == null ? 10 : dto.getPageSize()
        );
        return installationMapper.selectAdminList(
                page,
                dto.getStatus(),
                dto.getStartTime(),
                dto.getEndTime()
        );
    }

    // ─────────────────────────────────────────────
    // 确认预约（填写工程师信息）
    // ─────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmInstallation(Long adminId, Long id, InstallationConfirmDTO dto) {
        // ① 查询预约记录
        Installation installation = installationMapper.selectById(id);
        if (installation == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "预约记录不存在");

        }

        // ② 只能确认「待确认」状态的预约
        if (installation.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "只有待确认状态的预约才能确认");

        }

        // ③ 更新预约信息
        Installation update = new Installation();
        update.setId(id);
        update.setStatus(1); // 已确认
        update.setConfirmedTime(dto.getConfirmedTime());
        update.setEngineerName(dto.getEngineerName());
        update.setEngineerPhone(dto.getEngineerPhone());
        update.setAdminId(adminId);
        installationMapper.updateById(update);

        // ④ 发送通知给用户（MALL_INSTALLATION_CONFIRMED）
        // ⚠️ 需要在 NotificationHelper 中实现此方法，见说明文档
        String confirmedTimeStr = dto.getConfirmedTime() != null
                ? dto.getConfirmedTime().format(DT_FMT)
                : "待定";
        try {
            notificationHelper.sendInstallationConfirmedNotification(
                    installation.getUserId(),   // 接收通知的用户
                    dto.getEngineerName(),       // 工程师姓名
                    confirmedTimeStr,            // 确认上门时间（格式化字符串）
                    dto.getEngineerPhone(),      // 工程师联系方式
                    id                           // 预约ID（related_id）
            );
        } catch (Exception e) {
            // 通知发送失败不影响主业务
            log.error("安装预约确认通知发送失败: installationId={}, error={}", id, e.getMessage());
        }

        log.info("管理员确认安装预约: adminId={}, installationId={}", adminId, id);
    }

    // ─────────────────────────────────────────────
    // 取消预约（管理员端）
    // ─────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInstallation(Long adminId, Long id, InstallationAdminCancelDTO dto) {
        // ① 查询预约记录
        Installation installation = installationMapper.selectById(id);
        if (installation == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "预约记录不存在");
        }

        // ② 已取消的预约不能重复取消
        if (installation.getStatus() == 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该预约已处于取消状态");
        }

        // ③ 更新为已取消，记录取消原因
        Installation update = new Installation();
        update.setId(id);
        update.setStatus(2); // 已取消
        update.setAdminRemark(dto.getAdminRemark());
        update.setAdminId(adminId);
        installationMapper.updateById(update);

        log.info("管理员取消安装预约: adminId={}, installationId={}, reason={}",
                adminId, id, dto.getAdminRemark());

        // ④ 发送取消通知给用户（MALL_INSTALLATION_CANCELLED）
        notificationHelper.sendInstallationCancelledNotification(
                installation.getUserId(),
                dto.getAdminRemark(),
                installation.getId()
        );
    }
}
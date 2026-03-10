package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.InstallationAdminCancelDTO;
import com.astronomy.mall.module.admin.dto.InstallationConfirmDTO;
import com.astronomy.mall.module.admin.dto.InstallationQueryDTO;
import com.astronomy.mall.module.admin.vo.AdminInstallationVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 管理员安装预约 Service 接口
 *
 * 📌 文件路径:
 *   module/admin/service/AdminInstallationService.java
 *
 * 📌 接口对应:
 *   GET  /api/admin/installation/list          - getList
 *   POST /api/admin/installation/confirm/{id}  - confirmInstallation
 *   POST /api/admin/installation/cancel/{id}   - cancelInstallation
 */
public interface AdminInstallationService {

    /**
     * 查询安装预约列表（分页+筛选）
     *
     * @param dto 查询条件（状态、时间范围、分页）
     * @return 分页预约列表
     */
    IPage<AdminInstallationVO> getList(InstallationQueryDTO dto);

    /**
     * 确认安装预约（填写工程师信息并发送通知）
     *
     * 📌 确认后向用户发送 MALL_INSTALLATION_CONFIRMED 通知
     *
     * @param adminId 当前操作的管理员ID
     * @param id      预约ID
     * @param dto     确认信息（工程师姓名、电话、上门时间）
     */
    void confirmInstallation(Long adminId, Long id, InstallationConfirmDTO dto);

    /**
     * 取消安装预约（管理员端）
     *
     * 📌 可以取消状态=0(待确认) 或 状态=1(已确认) 的预约
     *
     * @param adminId 当前操作的管理员ID
     * @param id      预约ID
     * @param dto     取消原因
     */
    void cancelInstallation(Long adminId, Long id, InstallationAdminCancelDTO dto);
}
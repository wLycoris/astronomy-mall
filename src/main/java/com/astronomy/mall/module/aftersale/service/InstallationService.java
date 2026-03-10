package com.astronomy.mall.module.aftersale.service;

import com.astronomy.mall.module.aftersale.dto.InstallationApplyDTO;
import com.astronomy.mall.module.aftersale.entity.Installation;
import com.astronomy.mall.module.aftersale.vo.InstallationVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 安装预约 Service 接口（用户端）
 *
 * 📌 接口对应:
 *   POST   /api/installation/submit       - submitInstallation
 *   GET    /api/installation/my/list      - getMyList
 *   POST   /api/installation/cancel/:id   - cancelInstallation
 */
public interface InstallationService extends IService<Installation> {

    /**
     * 提交安装预约
     *
     * 前置校验（后端必须执行）:
     *   1. 订单必须属于当前用户（防越权）
     *   2. 订单状态必须为 2(待收货) 或 3(已完成)
     *   3. 同一订单不能重复提交预约
     *   4. productId 必须是该订单内的商品
     *
     * @param userId 当前用户ID
     * @param dto    提交预约DTO
     */
    void submitInstallation(Long userId, InstallationApplyDTO dto);

    /**
     * 查询我的预约列表（分页）
     *
     * @param userId   当前用户ID
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @return 分页预约列表
     */
    IPage<InstallationVO> getMyList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 取消安装预约（用户端）
     *
     * 📌 只允许取消状态=0(待确认)的预约
     * 📌 必须校验预约归属于当前用户
     *
     * @param userId 当前用户ID
     * @param id     预约ID
     */
    void cancelInstallation(Long userId, Long id);
}
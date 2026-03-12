package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.vo.AdminRecyclingVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 管理员端 - 二手回收服务接口
 *
 * 📌 提供管理员侧 5 个核心操作:
 *   1. 回收申请列表（分页+筛选）
 *   2. 提交报价
 *   3. 拒绝申请
 *   4. 安排取件（填写快递信息）
 *   5. 标记已回收 → 自动发放余额
 */
public interface AdminRecyclingService {

    /**
     * 回收申请列表（分页）
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<AdminRecyclingVO> getList(RecyclingQueryDTO dto);

    /**
     * 查看申请详情
     *
     * @param id 申请ID
     * @return 详情 VO
     */
    AdminRecyclingVO getDetail(Long id);

    /**
     * 提交报价（待审核 → 已报价）
     *
     * @param id      申请ID
     * @param dto     报价信息
     * @param adminId 操作管理员ID
     */
    void submitQuote(Long id, RecyclingQuoteDTO dto, Long adminId);

    /**
     * 拒绝申请（待审核 → 已拒绝）
     *
     * @param id      申请ID
     * @param dto     拒绝原因
     * @param adminId 操作管理员ID
     */
    void rejectApply(Long id, RecyclingRejectDTO dto, Long adminId);

    /**
     * 安排取件（用户确认 → 待取件）
     * 填写快递公司和快递单号
     *
     * @param id      申请ID
     * @param dto     快递信息
     * @param adminId 操作管理员ID
     */
    void arrangePickup(Long id, RecyclingArrangeDTO dto, Long adminId);

    /**
     * 标记已回收（待取件 → 已回收）
     * 触发余额自动发放到用户钱包
     *
     * @param id      申请ID
     * @param adminId 操作管理员ID
     */
    void completeRecycling(Long id, Long adminId);
}
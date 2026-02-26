package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.RefundAuditDTO;
import com.astronomy.mall.module.admin.dto.RefundQueryDTO;
import com.astronomy.mall.module.admin.vo.AdminRefundDetailVO;
import com.astronomy.mall.module.admin.vo.AdminRefundVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 后台退款管理Service
 */
public interface AdminRefundService {

    /**
     * 退款列表（分页）
     */
    Page<AdminRefundVO> getRefundList(RefundQueryDTO queryDTO);

    /**
     * 退款详情（含订单/用户/支付/商品信息）
     */
    AdminRefundDetailVO getRefundDetail(Long id);

    /**
     * 审核通过 —— 同时自动触发退款处理
     */
    void approveRefund(Long id, RefundAuditDTO auditDTO);

    /**
     * 审核拒绝
     */
    void rejectRefund(Long id, RefundAuditDTO auditDTO);

    /**
     * 手动处理退款（用于退款失败后重试）
     */
    void processRefund(Long id);
}
package com.astronomy.mall.module.payment.service;

import com.astronomy.mall.module.payment.dto.ApplyRefundDTO;
import com.astronomy.mall.module.payment.vo.RefundVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 退款服务接口
 */
public interface RefundService {

    /**
     * 申请退款
     */
    RefundVO applyRefund(ApplyRefundDTO dto, Long userId);

    /**
     * 查询退款详情
     */
    RefundVO getRefundDetail(Long refundId, Long userId);

    /**
     * 根据订单ID查询退款记录
     */
    RefundVO getRefundByOrderId(Long orderId, Long userId);

    /**
     * 分页查询退款列表
     */
    Page<RefundVO> getRefundList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 取消退款申请
     */
    void cancelRefund(Long refundId, Long userId);
}
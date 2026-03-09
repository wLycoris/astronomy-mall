package com.astronomy.mall.module.payment.service;

import com.astronomy.mall.module.payment.dto.ApplyRefundDTO;
import com.astronomy.mall.module.payment.vo.RefundVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 退款服务接口
 */
public interface RefundService {

    /** 申请退款 */
    RefundVO applyRefund(ApplyRefundDTO dto, Long userId);

    /** 查询退款详情 */
    RefundVO getRefundDetail(Long refundId, Long userId);

    /** 根据订单ID查询退款记录 */
    RefundVO getRefundByOrderId(Long orderId, Long userId);

    /** 分页查询退款列表（用户端） */
    Page<RefundVO> getRefundList(Long userId, Integer pageNum, Integer pageSize);

    /** 取消退款申请 */
    void cancelRefund(Long refundId, Long userId);

    /**
     * 审核退款（管理端调用）
     *
     * 2.4.4 新增：审核通过时判断原支付方式：
     *   - 余额支付（paymentType=3）→ 退款金额加回钱包，写流水
     *   - 其他支付方式           → 原渠道退款（模拟成功即可）
     *
     * @param refundId    退款ID
     * @param approved    true=通过  false=拒绝
     * @param adminRemark 审核备注
     */
    void auditRefund(Long refundId, boolean approved, String adminRemark);
}
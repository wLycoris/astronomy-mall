package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.ReviewAuditDTO;
import com.astronomy.mall.module.admin.dto.ReviewQueryDTO;
import com.astronomy.mall.module.admin.dto.ReviewReplyDTO;
import com.astronomy.mall.module.admin.vo.AdminReviewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 后台评价管理 Service 接口
 */
public interface AdminReviewService {

    /**
     * 评价列表（分页）
     * GET /api/admin/review/list
     *
     * @param dto 查询条件（商品ID、评分、状态、关键词等）
     * @return 分页结果
     */
    Page<AdminReviewVO> getReviewList(ReviewQueryDTO dto);

    /**
     * 评价详情
     * GET /api/admin/review/detail/:id
     *
     * @param id 评价ID
     * @return 评价详情（含商品/用户/订单冗余信息）
     */
    AdminReviewVO getReviewDetail(Long id);

    /**
     * 商家回复评价
     * POST /api/admin/review/reply/:id
     *
     * @param id  评价ID
     * @param dto 回复内容
     */
    void replyReview(Long id, ReviewReplyDTO dto);

    /**
     * 审核评价
     * POST /api/admin/review/audit/:id
     * - action=1: 审核通过（待审核→正常）
     * - action=0: 删除/拒绝（待审核→已删除）
     *
     * @param id  评价ID
     * @param dto 审核动作及备注
     */
    void auditReview(Long id, ReviewAuditDTO dto);

    /**
     * 置顶/取消置顶评价
     * POST /api/admin/review/top/:id
     *
     * @param id 评价ID
     */
    void toggleTopReview(Long id);

    /**
     * 删除评价（逻辑删除）
     * DELETE /api/admin/review/delete/:id
     *
     * @param id 评价ID
     */
    void deleteReview(Long id);
}
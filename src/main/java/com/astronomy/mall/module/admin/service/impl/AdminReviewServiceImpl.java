package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.ReviewAuditDTO;
import com.astronomy.mall.module.admin.dto.ReviewQueryDTO;
import com.astronomy.mall.module.admin.dto.ReviewReplyDTO;
import com.astronomy.mall.module.admin.entity.ReviewEntity;
import com.astronomy.mall.module.admin.mapper.AdminReviewMapper;
import com.astronomy.mall.module.admin.vo.AdminReviewVO;
import com.astronomy.mall.module.admin.service.AdminReviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AdminReviewServiceImpl implements AdminReviewService {

    @Autowired
    private AdminReviewMapper adminReviewMapper;

    // ============================================================
    // 1. 评价列表（分页）
    // ============================================================
    @Override
    public Page<AdminReviewVO> getReviewList(ReviewQueryDTO dto) {
        Page<AdminReviewVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<AdminReviewVO> list = adminReviewMapper.selectReviewPage(page, dto);
        page.setRecords(list);
        return page;
    }

    // ============================================================
    // 2. 评价详情
    // ============================================================
    @Override
    public AdminReviewVO getReviewDetail(Long id) {
        AdminReviewVO vo = adminReviewMapper.selectReviewDetail(id);
        if (vo == null) {
            throw new BusinessException("评价不存在");
        }
        return vo;
    }

    // ============================================================
    // 3. 商家回复评价
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(Long id, ReviewReplyDTO dto) {
        ReviewEntity review = adminReviewMapper.selectById(id);
        if (review == null || Integer.valueOf(1).equals(review.getDeleted())) {
            throw new BusinessException("评价不存在");
        }
        if (Integer.valueOf(0).equals(review.getStatus())) {
            throw new BusinessException("该评价已被删除，无法回复");
        }

        review.setReply(dto.getReply());
        review.setReplyTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        adminReviewMapper.updateById(review);

        log.info("[评价管理] 回复评价成功, reviewId={}", id);
    }

    // ============================================================
    // 4. 审核评价
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReview(Long id, ReviewAuditDTO dto) {
        ReviewEntity review = adminReviewMapper.selectById(id);
        if (review == null || Integer.valueOf(1).equals(review.getDeleted())) {
            throw new BusinessException("评价不存在");
        }
        if (!Integer.valueOf(2).equals(review.getStatus())) {
            throw new BusinessException("该评价不在待审核状态，无法审核");
        }

        if (Integer.valueOf(1).equals(dto.getAction())) {
            review.setStatus(1);
            log.info("[评价管理] 审核通过, reviewId={}", id);
        } else {
            review.setStatus(0);
            review.setDeleted(1);
            log.info("[评价管理] 审核拒绝, reviewId={}", id);
        }
        review.setUpdateTime(LocalDateTime.now());
        adminReviewMapper.updateById(review);
    }

    // ============================================================
    // 5. 置顶 / 取消置顶
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTopReview(Long id) {
        ReviewEntity review = adminReviewMapper.selectById(id);
        if (review == null || Integer.valueOf(1).equals(review.getDeleted())) {
            throw new BusinessException("评价不存在");
        }
        if (!Integer.valueOf(1).equals(review.getStatus())) {
            throw new BusinessException("只有状态正常的评价才能置顶");
        }

        if (Integer.valueOf(1).equals(review.getIsTop())) {
            // 取消置顶：清空置顶时间
            review.setIsTop(0);
            review.setTopTime(null);
        } else {
            // 设置置顶：记录置顶时间，用于多条置顶时按时间排序
            review.setIsTop(1);
            review.setTopTime(LocalDateTime.now());
        }
        review.setUpdateTime(LocalDateTime.now());
        adminReviewMapper.updateById(review);

        log.info("[评价管理] 置顶切换, reviewId={}, isTop={}", id, review.getIsTop());
    }

    // ============================================================
    // 6. 删除评价（逻辑删除）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long id) {
        ReviewEntity review = adminReviewMapper.selectById(id);
        if (review == null || Integer.valueOf(1).equals(review.getDeleted())) {
            throw new BusinessException("评价不存在或已被删除");
        }

        review.setStatus(0);
        // 不设 deleted=1，保留记录让用户在"我的评价"看到"已被管理员删除"提示
        // deleted=1 只用于用户自己删除（物理删除走 deleteById）
        review.setUpdateTime(LocalDateTime.now());
        adminReviewMapper.updateById(review);

        log.info("[评价管理] 删除评价成功, reviewId={}", id);
    }
}
package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.*;
import com.astronomy.mall.module.admin.service.AdminRecyclingService;
import com.astronomy.mall.module.admin.vo.AdminRecyclingVO;
import com.astronomy.mall.module.aftersale.entity.Recycling;
import com.astronomy.mall.module.aftersale.mapper.RecyclingMapper;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import com.astronomy.mall.module.user.entity.User;
import com.astronomy.mall.module.user.mapper.UserMapper;
import com.astronomy.mall.module.user.service.BalanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 管理员端 - 二手回收服务实现
 *
 * 📌 包路径: com.astronomy.mall.module.admin.service.impl
 * 📌 核心依赖: BalanceService (user模块) - 回收完成时发放余额
 * 📌 通知集成: NotificationHelper.sendRecyclingCompleteNotification()
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRecyclingServiceImpl implements AdminRecyclingService {

    private final RecyclingMapper recyclingMapper;
    private final UserMapper userMapper;
    private final BalanceService balanceService;
    private final NotificationHelper notificationHelper;

    // ===================== 申请列表 =====================

    @Override
    public Page<AdminRecyclingVO> getList(RecyclingQueryDTO dto) {
        Page<Recycling> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<Recycling> wrapper = new LambdaQueryWrapper<Recycling>()
                .eq(dto.getStatus() != null, Recycling::getStatus, dto.getStatus())
                .eq(dto.getUserId() != null, Recycling::getUserId, dto.getUserId())
                .like(StringUtils.hasText(dto.getProductName()), Recycling::getProductName, dto.getProductName())
                .like(StringUtils.hasText(dto.getRecycleNo()), Recycling::getRecycleNo, dto.getRecycleNo())
                .orderByDesc(Recycling::getCreateTime);

        Page<Recycling> resultPage = recyclingMapper.selectPage(page, wrapper);

        // 转换为 VO（包含用户信息）
        Page<AdminRecyclingVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    // ===================== 申请详情 =====================

    @Override
    public AdminRecyclingVO getDetail(Long id) {
        Recycling recycling = recyclingMapper.selectById(id);
        if (recycling == null) {
            throw new BusinessException("回收申请不存在");
        }
        return convertToVO(recycling);
    }

    // ===================== 提交报价 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitQuote(Long id, RecyclingQuoteDTO dto, Long adminId) {
        Recycling recycling = getAndValidate(id);

        // 只有"待审核(0)"状态才能报价
        if (!Integer.valueOf(0).equals(recycling.getStatus())) {
            throw new BusinessException("只有【待审核】状态的申请才能提交报价");
        }

        recycling.setStatus(1);  // 已报价
        recycling.setAssessedPrice(dto.getAssessedPrice());
        recycling.setAdminRemark(dto.getAdminRemark());
        recycling.setAdminId(adminId);
        recyclingMapper.updateById(recycling);

        log.info("管理员 {} 为回收申请 {} 提交报价: ¥{}", adminId, id, dto.getAssessedPrice());
    }

    // ===================== 拒绝申请 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectApply(Long id, RecyclingRejectDTO dto, Long adminId) {
        Recycling recycling = getAndValidate(id);

        // 只有"待审核(0)"状态才能拒绝
        if (!Integer.valueOf(0).equals(recycling.getStatus())) {
            throw new BusinessException("只有【待审核】状态的申请才能拒绝");
        }

        recycling.setStatus(5);  // 已拒绝
        recycling.setAdminRemark(dto.getAdminRemark());
        recycling.setAdminId(adminId);
        recyclingMapper.updateById(recycling);

        log.info("管理员 {} 拒绝回收申请 {}, 原因: {}", adminId, id, dto.getAdminRemark());
    }

    // ===================== 安排取件 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arrangePickup(Long id, RecyclingArrangeDTO dto, Long adminId) {
        Recycling recycling = getAndValidate(id);

        // 只有"用户确认(2)"状态才能安排取件
        if (!Integer.valueOf(2).equals(recycling.getStatus())) {
            throw new BusinessException("只有【用户确认】状态才能安排取件");
        }

        recycling.setStatus(3);  // 待取件
        recycling.setLogisticsCompany(dto.getLogisticsCompany());
        recycling.setTrackingNumber(dto.getTrackingNumber());
        recycling.setAdminId(adminId);
        recyclingMapper.updateById(recycling);

        log.info("管理员 {} 为回收申请 {} 安排取件: {} - {}",
                adminId, id, dto.getLogisticsCompany(), dto.getTrackingNumber());
    }

    // ===================== 标记已回收（核心：自动发放余额） =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRecycling(Long id, Long adminId) {
        Recycling recycling = getAndValidate(id);

        // 必须是"待取件(3)"状态才能标记完成
        if (!Integer.valueOf(3).equals(recycling.getStatus())) {
            throw new BusinessException("状态不正确，请先安排取件（需处于【待取件】状态）");
        }

        // 确保已填报价金额
        if (recycling.getAssessedPrice() == null) {
            throw new BusinessException("尚未填写报价金额，无法完成回收");
        }

        // 1. 调用 BalanceService 发放余额（原子操作 + 自动记录流水）
        //    type=3 回收入账; relatedType="recycling"
        balanceService.changeBalance(
                recycling.getUserId(),
                recycling.getAssessedPrice(),        // 正数 = 收入
                3,                                    // type=3 回收入账
                "二手回收: " + recycling.getRecycleNo(),
                recycling.getId(),
                "recycling"
        );
        log.info("回收余额已发放: userId={}, amount={}, recycleNo={}",
                recycling.getUserId(), recycling.getAssessedPrice(), recycling.getRecycleNo());

        // 2. 更新回收申请状态为已回收(4)
        recycling.setStatus(4);
        recycling.setCompleteTime(LocalDateTime.now());
        recycling.setAdminId(adminId);
        recyclingMapper.updateById(recycling);

        // 3. 发送"回收款已到账"通知（异步）
        try {
            notificationHelper.sendRecyclingCompleteNotification(
                    recycling.getUserId(),
                    recycling.getRecycleNo(),
                    recycling.getAssessedPrice(),
                    recycling.getId()
            );
        } catch (Exception e) {
            // 通知失败不影响主流程
            log.warn("发送回收完成通知失败: recyclingId={}, error={}", id, e.getMessage());
        }

        log.info("管理员 {} 完成回收申请 {}, 余额 ¥{} 已到账用户 {}",
                adminId, id, recycling.getAssessedPrice(), recycling.getUserId());
    }

    // ===================== 私有工具方法 =====================

    /**
     * 获取并校验回收申请是否存在
     */
    private Recycling getAndValidate(Long id) {
        Recycling recycling = recyclingMapper.selectById(id);
        if (recycling == null) {
            throw new BusinessException("回收申请不存在");
        }
        return recycling;
    }

    /**
     * 实体转 AdminRecyclingVO（含用户信息）
     */
    private AdminRecyclingVO convertToVO(Recycling recycling) {
        AdminRecyclingVO vo = new AdminRecyclingVO();
        BeanUtils.copyProperties(recycling, vo);

        // 状态中文描述
        vo.setStatusText(getStatusText(recycling.getStatus()));

        // 成色中文描述
        vo.setConditionLevelText(getConditionText(recycling.getConditionLevel()));

        // 填充用户信息
        try {
            User user = userMapper.selectById(recycling.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setPhone(user.getPhone());
            }
        } catch (Exception e) {
            log.warn("查询用户信息失败: userId={}", recycling.getUserId());
        }

        // 填充管理员名称
        if (recycling.getAdminId() != null) {
            try {
                User admin = userMapper.selectById(recycling.getAdminId());
                if (admin != null) {
                    vo.setAdminName(admin.getUsername());
                }
            } catch (Exception e) {
                log.warn("查询管理员信息失败: adminId={}", recycling.getAdminId());
            }
        }

        return vo;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已报价";
            case 2: return "已确认";
            case 3: return "待取件";
            case 4: return "已回收";
            case 5: return "已拒绝";
            case 6: return "用户取消";
            default: return "未知";
        }
    }

    private String getConditionText(String level) {
        if (level == null) return "";
        switch (level) {
            case "S": return "全新/几乎未使用";
            case "A": return "九成新，无明显磨损";
            case "B": return "七八成新，有轻微使用痕迹";
            case "C": return "六成以下，有明显使用痕迹";
            default: return level;
        }
    }
}
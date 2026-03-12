package com.astronomy.mall.module.aftersale.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.aftersale.dto.RecyclingApplyDTO;
import com.astronomy.mall.module.aftersale.entity.Recycling;
import com.astronomy.mall.module.aftersale.mapper.RecyclingMapper;
import com.astronomy.mall.module.aftersale.service.RecyclingService;
import com.astronomy.mall.module.aftersale.vo.RecyclingVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 二手回收服务实现（用户端）
 *
 * 📌 包路径: com.astronomy.mall.module.aftersale.service.impl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecyclingServiceImpl implements RecyclingService {

    private final RecyclingMapper recyclingMapper;

    // ===================== 提交申请 =====================

    @Override
    public RecyclingVO submitApply(Long userId, RecyclingApplyDTO dto) {
        // 构建申请实体
        Recycling recycling = new Recycling();
        BeanUtils.copyProperties(dto, recycling);
        recycling.setUserId(userId);
        recycling.setStatus(0);  // 待审核
        recycling.setRecycleNo(generateRecycleNo());

        recyclingMapper.insert(recycling);
        log.info("用户 {} 提交二手回收申请, 单号: {}", userId, recycling.getRecycleNo());

        return convertToVO(recycling);
    }

    // ===================== 我的申请列表 =====================

    @Override
    public Page<RecyclingVO> getMyList(Long userId, Integer pageNum, Integer pageSize) {
        Page<Recycling> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Recycling> wrapper = new LambdaQueryWrapper<Recycling>()
                .eq(Recycling::getUserId, userId)
                .orderByDesc(Recycling::getCreateTime);

        Page<Recycling> resultPage = recyclingMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<RecyclingVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    // ===================== 申请详情 =====================

    @Override
    public RecyclingVO getDetail(Long userId, Long id) {
        Recycling recycling = recyclingMapper.selectById(id);
        if (recycling == null) {
            throw new BusinessException("回收申请不存在");
        }
        // 校验归属（只能查看自己的）
        if (!recycling.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此申请");
        }
        return convertToVO(recycling);
    }

    // ===================== 确认报价 =====================

    @Override
    public void confirmQuote(Long userId, Long id) {
        Recycling recycling = getAndValidateOwner(userId, id);

        // 只有"已报价(1)"状态才能确认
        if (!Integer.valueOf(1).equals(recycling.getStatus())) {
            throw new BusinessException("当前状态不支持确认报价，需处于【已报价】状态");
        }

        recycling.setStatus(2);  // 用户确认
        recycling.setConfirmTime(LocalDateTime.now());
        recyclingMapper.updateById(recycling);

        log.info("用户 {} 确认回收报价, 申请ID: {}", userId, id);
    }

    // ===================== 拒绝报价 =====================

    @Override
    public void rejectQuote(Long userId, Long id) {
        Recycling recycling = getAndValidateOwner(userId, id);

        // 只有"已报价(1)"状态才能拒绝
        if (!Integer.valueOf(1).equals(recycling.getStatus())) {
            throw new BusinessException("当前状态不支持拒绝报价，需处于【已报价】状态");
        }

        // 拒绝报价 → 变为用户取消(6)
        recycling.setStatus(6);
        recyclingMapper.updateById(recycling);

        log.info("用户 {} 拒绝回收报价, 申请ID: {}", userId, id);
    }

    // ===================== 取消申请 =====================

    @Override
    public void cancelApply(Long userId, Long id) {
        Recycling recycling = getAndValidateOwner(userId, id);

        // 只有"待审核(0)"状态才能取消
        if (!Integer.valueOf(0).equals(recycling.getStatus())) {
            throw new BusinessException("只有【待审核】状态的申请才能取消");
        }

        recycling.setStatus(6);  // 用户取消
        recyclingMapper.updateById(recycling);

        log.info("用户 {} 取消回收申请, 申请ID: {}", userId, id);
    }

    // ===================== 私有工具方法 =====================

    /**
     * 获取回收申请并校验归属
     */
    private Recycling getAndValidateOwner(Long userId, Long id) {
        Recycling recycling = recyclingMapper.selectById(id);
        if (recycling == null) {
            throw new BusinessException("回收申请不存在");
        }
        if (!recycling.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此申请");
        }
        return recycling;
    }

    /**
     * 实体转 VO
     */
    private RecyclingVO convertToVO(Recycling recycling) {
        RecyclingVO vo = new RecyclingVO();
        BeanUtils.copyProperties(recycling, vo);
        vo.setStatusText(RecyclingVO.getStatusText(recycling.getStatus()));
        vo.setConditionLevelText(RecyclingVO.getConditionText(recycling.getConditionLevel()));
        return vo;
    }

    /**
     * 生成回收单号: RC + 时间戳 + 4位随机数
     * 例: RC202603111523001234
     */
    private String generateRecycleNo() {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 9000) + 1000;
        return "RC" + timeStr + random;
    }
}
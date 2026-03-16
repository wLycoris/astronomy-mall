package com.astronomy.mall.module.admin.service.impl;

import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.common.result.ResultCode;
import com.astronomy.mall.module.admin.dto.AnnouncementCreateDTO;
import com.astronomy.mall.module.admin.dto.AnnouncementQueryDTO;
import com.astronomy.mall.module.admin.mapper.AdminAnnouncementMapper;
import com.astronomy.mall.module.admin.service.AdminAnnouncementService;
import com.astronomy.mall.module.admin.vo.AnnouncementVO;
import com.astronomy.mall.module.notification.entity.Notification;
import com.astronomy.mall.module.notification.mapper.NotificationMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统公告 ServiceImpl（管理员后台）
 *
 * 📌 实现思路:
 * 一次公告 = 一批具有相同 related_id 的 tb_notification 记录
 *
 * 核心字段:
 *   - module        = "system"
 *   - type          = "announcement"
 *   - related_type  = "announcement"
 *   - related_id    = announcementGroupId（System.currentTimeMillis()，唯一标识一次公告）
 *   - jump_url      = "/notice/detail?id={announcementGroupId}"
 *
 * 文件路径: com.astronomy.mall.module.admin.service.impl.AdminAnnouncementServiceImpl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    /** 公告相关查询 Mapper（GROUP BY 聚合、软删除等） */
    private final AdminAnnouncementMapper adminAnnouncementMapper;

    /** 通知 Mapper（insertBatch 批量写入通知记录） */
    private final NotificationMapper notificationMapper;

    // ========================================================
    // 批量插入阈值：超过此数量时分批插入，防止 SQL 过大导致 OOM
    // ========================================================
    private static final int BATCH_SIZE = 500;

    /**
     * 创建并发送公告
     *
     * 流程：
     * 1. 生成 announcementGroupId = System.currentTimeMillis()
     * 2. 查询所有活跃用户（status=1, deleted=0）
     * 3. 构建 Notification 列表（每个用户一条）
     * 4. 分批 insertBatch 写入数据库
     * 5. 返回 AnnouncementVO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO createAnnouncement(AnnouncementCreateDTO dto, Long adminId) {

        // ── Step 1: 生成公告分组ID ──────────────────────────────────
        // 用毫秒时间戳作为唯一分组ID，管理后台发公告极少并发，足够唯一。
        // 若担心并发冲突，可改用 Math.abs(UUID.randomUUID().getMostSignificantBits())
        long announcementGroupId = System.currentTimeMillis();

        // ── Step 2: 查询所有活跃用户ID ─────────────────────────────
        List<Long> userIds = adminAnnouncementMapper.selectAllActiveUserIds();
        if (userIds == null || userIds.isEmpty()) {
            log.warn("[公告创建] 当前系统无活跃用户，公告未发送");
            // 即使无用户，也视为成功（插入0条）
            AnnouncementVO vo = new AnnouncementVO();
            vo.setAnnouncementId(announcementGroupId);
            vo.setTitle(dto.getTitle());
            vo.setContent(dto.getContent());
            vo.setPriority(dto.getPriority());
            vo.setPriorityText(buildPriorityText(dto.getPriority()));
            vo.setSendCount(0);
            vo.setReadCount(0);
            vo.setReadRate(0.0);
            vo.setCreateTime(LocalDateTime.now());
            return vo;
        }

        log.info("[公告创建] 开始发送公告: title={}, userCount={}, announcementId={}",
                dto.getTitle(), userIds.size(), announcementGroupId);

        // ── Step 3: 构建通知列表 ───────────────────────────────────
        String jumpUrl = "/notice/detail?id=" + announcementGroupId;
        LocalDateTime now = LocalDateTime.now();
        // 优先级保证不为 null
        int priority = dto.getPriority() == null ? 0 : dto.getPriority();

        // ── Step 4: 分批插入（每批 BATCH_SIZE 条）────────────────────
        int total = userIds.size();
        int insertedCount = 0;

        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<Long> batchIds = userIds.subList(i, end);

            List<Notification> batch = new ArrayList<>(batchIds.size());
            for (Long userId : batchIds) {
                Notification n = new Notification();
                n.setUserId(userId);
                n.setModule("system");
                n.setType("announcement");
                n.setTitle(dto.getTitle());
                n.setContent(dto.getContent());
                n.setJumpUrl(jumpUrl);
                n.setRelatedId(announcementGroupId);
                n.setRelatedType("announcement");
                n.setPriority(priority);
                n.setIsRead(0);
                n.setDeleted(0);
                n.setCreateTime(now);
                batch.add(n);
            }

            notificationMapper.insertBatch(batch);
            insertedCount += batch.size();

            log.debug("[公告创建] 已插入 {}/{} 条通知", insertedCount, total);
        }

        log.info("[公告创建] 公告发送完成: announcementId={}, totalSent={}", announcementGroupId, insertedCount);

        // ── Step 5: 构建返回 VO ────────────────────────────────────
        AnnouncementVO vo = new AnnouncementVO();
        vo.setAnnouncementId(announcementGroupId);
        vo.setTitle(dto.getTitle());
        vo.setContent(dto.getContent());
        vo.setPriority(priority);
        vo.setPriorityText(buildPriorityText(priority));
        vo.setSendCount(insertedCount);
        vo.setReadCount(0);
        vo.setReadRate(0.0);
        vo.setCreateTime(now);
        return vo;
    }

    /**
     * 分页查询公告列表
     *
     * 使用 AdminAnnouncementMapper.selectAnnouncementPage 执行 GROUP BY 聚合查询。
     * 返回 MyBatis-Plus Page 对象（前端使用 res.data.records 和 res.data.total）。
     */
    @Override
    public Page<AnnouncementVO> listAnnouncements(AnnouncementQueryDTO dto) {
        // 参数防御
        if (dto.getPageNum() == null || dto.getPageNum() < 1) dto.setPageNum(1);
        if (dto.getPageSize() == null || dto.getPageSize() < 1) dto.setPageSize(10);

        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        int limit  = dto.getPageSize();

        // 查询数据 & 总数
        List<AnnouncementVO> records = adminAnnouncementMapper.selectAnnouncementPage(dto, offset, limit);
        Long total = adminAnnouncementMapper.countAnnouncements(dto);

        // 组装 Page（前端 res.data.records / res.data.total 标准结构）
        Page<AnnouncementVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        page.setRecords(records);
        page.setTotal(total == null ? 0 : total);
        return page;
    }

    /**
     * 查询公告详情
     */
    @Override
    public AnnouncementVO getAnnouncementDetail(Long announcementId) {
        AnnouncementVO vo = adminAnnouncementMapper.selectAnnouncementById(announcementId);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "公告不存在或已被删除");
        }
        return vo;
    }

    /**
     * 删除公告（软删除）
     *
     * 将该公告对应的所有 tb_notification 记录的 deleted 设为 1。
     * 用户通知列表会因 deleted=1 自动过滤，已读通知也同步删除，对用户无感。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long announcementId) {
        // 先查询是否存在（避免误删）
        AnnouncementVO vo = adminAnnouncementMapper.selectAnnouncementById(announcementId);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "公告不存在或已被删除");
        }

        int affectedRows = adminAnnouncementMapper.softDeleteAnnouncement(announcementId);
        log.info("[公告删除] announcementId={}, 受影响行数={}", announcementId, affectedRows);
    }

    // ────────────────────────────────────────────
    // 私有辅助方法
    // ────────────────────────────────────────────

    /**
     * 将优先级数字转为展示文本
     *
     * @param priority 优先级数字（0-普通 1-重要 2-紧急）
     * @return 展示文本
     */
    private String buildPriorityText(int priority) {
        switch (priority) {
            case 1: return "重要";
            case 2: return "紧急";
            default: return "普通";
        }
    }
}
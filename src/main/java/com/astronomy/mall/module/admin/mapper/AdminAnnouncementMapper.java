package com.astronomy.mall.module.admin.mapper;

import com.astronomy.mall.module.admin.dto.AnnouncementQueryDTO;
import com.astronomy.mall.module.admin.vo.AnnouncementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统公告 Mapper（管理员后台）
 *
 * 📌 设计说明:
 * 公告数据存储在 tb_notification 表中，不新建 tb_announcement 表。
 * 通过 GROUP BY n.related_id 聚合展示公告列表。
 *
 * 涉及表: tb_notification
 * 筛选条件: module='system' AND type='announcement' AND related_type='announcement' AND deleted=0
 *
 * 文件路径: com.astronomy.mall.module.admin.mapper.AdminAnnouncementMapper
 */
@Mapper
public interface AdminAnnouncementMapper {

    /**
     * 分页查询公告列表（GROUP BY related_id 聚合）
     *
     * @param dto    查询条件（关键词、时间范围）
     * @param offset 偏移量（pageNum-1）* pageSize
     * @param limit  每页数量
     * @return 公告VO列表
     */
    List<AnnouncementVO> selectAnnouncementPage(
            @Param("dto") AnnouncementQueryDTO dto,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 查询公告总数（用于分页计算）
     *
     * @param dto 查询条件
     * @return 公告总数
     */
    Long countAnnouncements(@Param("dto") AnnouncementQueryDTO dto);

    /**
     * 查询单个公告详情（通过 related_id 聚合）
     *
     * @param announcementId 公告ID（即 related_id）
     * @return 公告VO，不存在时返回 null
     */
    AnnouncementVO selectAnnouncementById(@Param("announcementId") Long announcementId);

    /**
     * 软删除公告（将公告对应的所有通知记录设为 deleted=1）
     *
     * @param announcementId 公告ID（即 related_id）
     * @return 受影响行数
     */
    int softDeleteAnnouncement(@Param("announcementId") Long announcementId);

    /**
     * 查询所有启用状态的用户ID（用于批量发送公告通知）
     *
     * @return 用户ID列表（status=1 且 deleted=0 的全部用户）
     */
    List<Long> selectAllActiveUserIds();
}
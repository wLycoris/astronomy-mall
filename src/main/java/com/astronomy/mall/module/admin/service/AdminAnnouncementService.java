package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.AnnouncementCreateDTO;
import com.astronomy.mall.module.admin.dto.AnnouncementQueryDTO;
import com.astronomy.mall.module.admin.vo.AnnouncementVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 系统公告 Service 接口（管理员后台）
 *
 * 📌 设计说明:
 * 不新建 tb_announcement 表，直接复用 tb_notification 表。
 * 公告 = 批量发送给所有用户的系统通知（type='announcement', module='system'）。
 * 用 related_id 作为"公告分组ID"，通过 GROUP BY 聚合展示。
 *
 * 文件路径: com.astronomy.mall.module.admin.service.AdminAnnouncementService
 */
public interface AdminAnnouncementService {

    /**
     * 创建并发送公告
     * 1. 生成唯一公告分组ID（System.currentTimeMillis()）
     * 2. 查询所有活跃用户ID
     * 3. 批量写入 tb_notification（每个用户一条）
     * 4. 返回公告VO（含发送数量）
     *
     * @param dto     公告创建请求（标题+内容+优先级）
     * @param adminId 操作的管理员ID
     * @return 创建完成的公告VO
     */
    AnnouncementVO createAnnouncement(AnnouncementCreateDTO dto, Long adminId);

    /**
     * 分页查询公告列表
     * 通过 GROUP BY related_id 去重，每行代表一次公告
     *
     * @param dto 查询条件（关键词、时间范围、分页）
     * @return 分页结果（MyBatis-Plus Page<AnnouncementVO>）
     */
    Page<AnnouncementVO> listAnnouncements(AnnouncementQueryDTO dto);

    /**
     * 查询公告详情
     *
     * @param announcementId 公告ID（即 tb_notification.related_id）
     * @return 公告VO（含已读率等统计数据）
     * @throws com.astronomy.mall.common.exception.BusinessException 公告不存在时抛出
     */
    AnnouncementVO getAnnouncementDetail(Long announcementId);

    /**
     * 删除公告（软删除）
     * 将该公告对应的所有 tb_notification 记录的 deleted 设为 1
     *
     * @param announcementId 公告ID（即 tb_notification.related_id）
     * @throws com.astronomy.mall.common.exception.BusinessException 公告不存在时抛出
     */
    void deleteAnnouncement(Long announcementId);
}
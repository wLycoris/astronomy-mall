package com.astronomy.mall.module.admin.service;

import com.astronomy.mall.module.admin.dto.PostAuditDTO;
import com.astronomy.mall.module.admin.dto.PostCommentQueryDTO;
import com.astronomy.mall.module.admin.dto.PostQueryDTO;
import com.astronomy.mall.module.admin.vo.ForumStatsVO;

import java.util.Map;

/**
 * 论坛后台管理 Service 接口（7.7）
 *
 * 📌 8 个方法对应 AdminPostController 的 8 个端点:
 *   1. listPosts          - 帖子列表（分页+状态+关键词筛选）
 *   2. auditPost          - 审核帖子（通过/拒绝 + 通知）
 *   3. toggleRecommendPost - 加入/取消推荐（管理员推荐信号，供 8.x 推荐算法加权）
 *   4. deletePost         - 删除帖子（status=4 标记，保留记录）
 *   5. listComments       - 评论列表（分页+按帖子筛选）
 *   6. deleteComment      - 删除评论（@TableLogic 软删 deleted=1）
 *   7. getStats           - 论坛数据统计（4个总数+状态分布+7天趋势）
 *   8. getPendingCount    - 待审核帖子数（导航角标）
 *
 * 📌 设计要点:
 *   - 所有写操作 @Transactional + AdminLog AOP
 *   - 审核成功后通过 NotificationHelper 发送 POST_APPROVED / POST_REJECTED
 *   - 删除帖子使用 status=4 而非 @TableLogic，让用户能看到"已被管理员删除"
 *   - 删除评论使用 @TableLogic（直接 deleted=1），用户主页消失即可
 */
public interface AdminPostService {

    /**
     * 1. 帖子列表（管理员视角，可查所有状态）
     */
    Map<String, Object> listPosts(PostQueryDTO dto);

    /**
     * 2. 审核帖子
     * @param postId  帖子ID
     * @param dto     {action: approve/reject, reason}
     * @param adminId 操作管理员ID（用于日志）
     */
    void auditPost(Long postId, PostAuditDTO dto, Long adminId);

    /**
     * 3. 加入/取消"管理员推荐"（幂等切换）
     *
     * 📌 7.7 重新定位:
     *   原"置顶"功能因小红书风格瀑布流无视觉效果而废弃。
     *   底层 DB 列 is_top 复用为"管理员推荐信号"——
     *   - 用户端列表/卡片完全不感知此字段
     *   - 8.x 推荐算法将其作为 hot_score 的额外加权因子
     *   - 字段名暂未在 DB 层面 rename，避免额外 migration
     */
    void toggleRecommendPost(Long postId, Long adminId);

    /**
     * 4. 删除帖子（标记 status=4，保留记录）
     */
    void deletePost(Long postId, Long adminId);

    /**
     * 5. 评论列表（管理员视角）
     */
    Map<String, Object> listComments(PostCommentQueryDTO dto);

    /**
     * 6. 删除评论（@TableLogic 软删）
     */
    void deleteComment(Long commentId, Long adminId);

    /**
     * 7. 论坛数据统计
     */
    ForumStatsVO getStats();

    /**
     * 8. 待审核帖子数量
     */
    long getPendingCount();
}

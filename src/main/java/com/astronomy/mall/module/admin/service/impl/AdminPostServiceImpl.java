package com.astronomy.mall.module.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.astronomy.mall.common.exception.BusinessException;
import com.astronomy.mall.module.admin.dto.PostAuditDTO;
import com.astronomy.mall.module.admin.dto.PostCommentQueryDTO;
import com.astronomy.mall.module.admin.dto.PostQueryDTO;
import com.astronomy.mall.module.admin.mapper.AdminPostCommentMapper;
import com.astronomy.mall.module.admin.mapper.AdminPostMapper;
import com.astronomy.mall.module.admin.service.AdminPostService;
import com.astronomy.mall.module.admin.vo.ForumStatsVO;
import com.astronomy.mall.module.forum.entity.Post;
import com.astronomy.mall.module.forum.entity.PostComment;
import com.astronomy.mall.module.forum.mapper.PostCommentMapper;
import com.astronomy.mall.module.forum.mapper.PostMapper;
import com.astronomy.mall.module.notification.helper.NotificationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 论坛后台管理 Service 实现（7.7）
 *
 * 📌 8 个方法对应 AdminPostController 的 8 个端点。
 * 📌 写操作均加 @Transactional，由 Controller 层 @AdminLog 注解触发审计日志。
 * 📌 通知触发: 审核成功后通过 NotificationHelper 异步发送 POST_APPROVED / POST_REJECTED。
 *
 * 状态机说明 (tb_post.status):
 *   0 草稿        - 用户保存的草稿（暂未使用）
 *   1 审核中      - 待管理员审核
 *   2 已发布      - 公开可见
 *   3 已拒绝      - 含拒绝原因，作者可重新编辑提交
 *   4 管理员删除  - 不再公开可见，但记录保留供作者查看"已被删除"提示
 */
@Slf4j
@Service
public class AdminPostServiceImpl implements AdminPostService {

    /** forum.PostMapper：仅用 BaseMapper 的 selectById/updateById 等通用 CRUD */
    @Autowired
    private PostMapper postMapper;

    /** forum.PostCommentMapper：仅用于 selectById/deleteById（@TableLogic 软删） */
    @Autowired
    private PostCommentMapper postCommentMapper;

    /** 7.7 后台帖子查询/统计专用 mapper（管理员视角） */
    @Autowired
    private AdminPostMapper adminPostMapper;

    /** 7.7 后台评论查询/统计专用 mapper（管理员视角） */
    @Autowired
    private AdminPostCommentMapper adminPostCommentMapper;

    @Autowired
    private NotificationHelper notificationHelper;

    /** 列表/统计 SQL 中常用的日期格式 */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ============================================================
    // 1. 帖子列表（管理员视角）
    // ============================================================
    @Override
    public Map<String, Object> listPosts(PostQueryDTO dto) {
        // 1. 参数兜底
        int pageNum = (dto.getPageNum() == null || dto.getPageNum() < 1) ? 1 : dto.getPageNum();
        int pageSize = (dto.getPageSize() == null || dto.getPageSize() < 1) ? 20 : dto.getPageSize();
        if (pageSize > 100) pageSize = 100;
        int offset = (pageNum - 1) * pageSize;

        // 2. 查列表 + 总数（走 admin 模块专用 mapper）
        List<Map<String, Object>> rows = adminPostMapper.selectAdminPostList(
                dto.getStatus(), dto.getKeyword(), offset, pageSize);
        long total = adminPostMapper.countAdminPostList(dto.getStatus(), dto.getKeyword());

        // 3. 解析 images JSON → coverImage（管理员列表只展示封面图，节省带宽）
        for (Map<String, Object> row : rows) {
            String imagesStr = row.get("images") == null ? null : row.get("images").toString();
            if (StringUtils.hasText(imagesStr)) {
                try {
                    List<String> imageList = JSON.parseArray(imagesStr, String.class);
                    if (imageList != null && !imageList.isEmpty()) {
                        row.put("coverImage", imageList.get(0));
                        row.put("imageCount", imageList.size());
                    }
                } catch (Exception e) {
                    log.warn("帖子图片JSON解析失败, postId={}", row.get("id"));
                }
            }
            // 解析 tags JSON → List<String>
            String tagsStr = row.get("tags") == null ? null : row.get("tags").toString();
            if (StringUtils.hasText(tagsStr)) {
                try {
                    row.put("tags", JSON.parseArray(tagsStr, String.class));
                } catch (Exception e) {
                    row.put("tags", Collections.emptyList());
                }
            }
            // 列表不返回完整正文（节省带宽 + 防 base64 大对象）
            String content = (String) row.get("content");
            if (content != null && content.length() > 100) {
                row.put("content", content.substring(0, 100) + "...");
            }
            // 移除 raw images 字段（base64 太大）
            row.remove("images");
        }

        // 4. 组装分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", rows);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    // ============================================================
    // 2. 审核帖子（通过/拒绝 + 通知）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPost(Long postId, PostAuditDTO dto, Long adminId) {
        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在");
        }

        // 2. 校验状态：仅审核中(status=1)的帖子可审核
        if (!Integer.valueOf(1).equals(post.getStatus())) {
            throw new BusinessException("该帖子不在待审核状态，无法审核");
        }

        // 3. 校验 action
        String action = dto.getAction();
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new BusinessException("审核动作非法，仅支持 approve / reject");
        }

        // 4. 拒绝时校验原因
        if ("reject".equals(action) && !StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("拒绝时必须填写原因");
        }

        // 5. 更新状态
        Post update = new Post();
        update.setId(postId);
        if ("approve".equals(action)) {
            update.setStatus(2);
            update.setRejectReason(""); // 清除可能残留的原因
        } else {
            update.setStatus(3);
            update.setRejectReason(dto.getReason().trim());
        }
        postMapper.updateById(update);
        log.info("[论坛后台] 管理员 {} 审核帖子 {}: action={}", adminId, postId, action);

        // 6. 异步发送通知（审核结果）
        try {
            if ("approve".equals(action)) {
                notificationHelper.sendPostApprovedNotification(
                        post.getUserId(), post.getTitle(), postId);
            } else {
                notificationHelper.sendPostRejectedNotification(
                        post.getUserId(), post.getTitle(), dto.getReason(), postId);
            }
        } catch (Exception e) {
            // 通知失败不影响审核主流程
            log.warn("[论坛后台] 审核通知发送失败: postId={}", postId, e);
        }
    }

    // ============================================================
    // 3. 加入 / 取消"管理员推荐"（幂等切换）
    //
    // 📌 7.7 重新定位:
    //   原 toggleTopPost (置顶/取消置顶) 改为 toggleRecommendPost。
    //   - 底层 DB 字段 is_top 复用为"推荐信号"
    //   - 用户端列表/卡片不再读取 is_top 做排序或视觉提示
    //   - 8.x 推荐算法会把 is_top=1 作为 hot_score 加权因子
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleRecommendPost(Long postId, Long adminId) {
        Post post = postMapper.selectById(postId);
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在");
        }
        // 仅"已发布"的帖子可推荐
        if (!Integer.valueOf(2).equals(post.getStatus())) {
            throw new BusinessException("仅已发布的帖子可加入/取消推荐");
        }

        Post update = new Post();
        update.setId(postId);
        // 0 → 1, 1 → 0
        update.setIsTop(Integer.valueOf(1).equals(post.getIsTop()) ? 0 : 1);
        postMapper.updateById(update);
        log.info("[论坛后台] 管理员 {} 切换帖子 {} 推荐状态: isRecommend={}",
                adminId, postId, update.getIsTop());
    }

    // ============================================================
    // 4. 删除帖子（status=4，保留记录）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long adminId) {
        Post post = postMapper.selectById(postId);
        if (post == null || Integer.valueOf(1).equals(post.getDeleted())) {
            throw new BusinessException("帖子不存在或已被删除");
        }
        if (Integer.valueOf(4).equals(post.getStatus())) {
            throw new BusinessException("该帖子已被管理员删除");
        }

        // 不走 @TableLogic deleted=1（那样作者完全看不到了），
        // 而是 status=4，让作者在"我的帖子"页能看到"已被管理员删除"的提示。
        Post update = new Post();
        update.setId(postId);
        update.setStatus(4);
        postMapper.updateById(update);
        log.info("[论坛后台] 管理员 {} 删除帖子 {}", adminId, postId);
    }

    // ============================================================
    // 5. 评论列表（管理员视角）
    // ============================================================
    @Override
    public Map<String, Object> listComments(PostCommentQueryDTO dto) {
        int pageNum = (dto.getPageNum() == null || dto.getPageNum() < 1) ? 1 : dto.getPageNum();
        int pageSize = (dto.getPageSize() == null || dto.getPageSize() < 1) ? 20 : dto.getPageSize();
        if (pageSize > 100) pageSize = 100;
        int offset = (pageNum - 1) * pageSize;

        List<Map<String, Object>> rows = adminPostCommentMapper.selectAdminCommentList(
                dto.getPostId(), dto.getKeyword(), offset, pageSize);
        long total = adminPostCommentMapper.countAdminCommentList(dto.getPostId(), dto.getKeyword());

        Map<String, Object> result = new HashMap<>();
        result.put("list", rows);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    // ============================================================
    // 6. 删除评论（@TableLogic 软删 + 帖子评论数 -1）
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long adminId) {
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null || Integer.valueOf(1).equals(comment.getDeleted())) {
            throw new BusinessException("评论不存在或已被删除");
        }

        // ── 删除目标评论本身（@TableLogic 软删，deleted 自动置 1）──
        postCommentMapper.deleteById(commentId);
        int deletedCount = 1;

        // ── 顶级评论级联删除所有子回复 ──
        // tb_post_comment.parent_id 始终指向"顶级评论"ID（两级评论结构），
        // 所以只需 parent_id == commentId 即可一次性扫到全部子回复。
        // 子回复同样走 @TableLogic 软删，未来若需恢复可 deleted=0 复原。
        if (Long.valueOf(0L).equals(comment.getParentId())) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostComment> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("parent_id", commentId)
                   .eq("deleted", 0);
            List<PostComment> children = postCommentMapper.selectList(wrapper);
            for (PostComment child : children) {
                postCommentMapper.deleteById(child.getId());
                deletedCount++;
            }
        }

        // ── 同步更新帖子评论数（顶级+子回复一起减）──
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            Post update = new Post();
            update.setId(post.getId());
            update.setCommentCount(Math.max(0, post.getCommentCount() - deletedCount));
            postMapper.updateById(update);
        }

        log.info("[论坛后台] 管理员 {} 删除评论 {}（所属帖子 {}，含级联子回复共 {} 条）",
                adminId, commentId, comment.getPostId(), deletedCount);
    }

    // ============================================================
    // 7. 论坛数据统计
    // ============================================================
    @Override
    public ForumStatsVO getStats() {
        ForumStatsVO vo = new ForumStatsVO();

        // ── 7.1 概览数字 ──
        vo.setTodayPostCount(adminPostMapper.countTodayPosts());
        vo.setTodayCommentCount(adminPostCommentMapper.countTodayComments());
        vo.setTodayActiveUsers(adminPostCommentMapper.countTodayActiveUsers());
        vo.setTotalPostCount(adminPostMapper.countTotalPosts());
        vo.setTotalCommentCount(adminPostCommentMapper.countTotalComments());
        vo.setTotalUserCount(adminPostMapper.countTotalAuthors());
        vo.setPendingCount(adminPostMapper.countPendingPosts());

        // ── 7.2 状态分布（饼图）──
        // 把 status 数字转成可读名称
        List<Map<String, Object>> rawDist = adminPostMapper.selectStatusDistribution();
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map<String, Object> row : rawDist) {
            int status = ((Number) row.get("status")).intValue();
            long count = ((Number) row.get("count")).longValue();
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusName(status));
            item.put("status", status);
            item.put("value", count);
            distribution.add(item);
        }
        vo.setStatusDistribution(distribution);

        // ── 7.3 近7天发帖+评论趋势（折线图）──
        // 先按 date 建索引，再补齐空白日期（保证图表X轴是连续7天）
        int days = 7;
        Map<String, Long> postMap = new HashMap<>();
        for (Map<String, Object> row : adminPostMapper.selectDailyPostCount(days)) {
            postMap.put(row.get("date").toString(), ((Number) row.get("count")).longValue());
        }
        Map<String, Long> commentMap = new HashMap<>();
        for (Map<String, Object> row : adminPostCommentMapper.selectDailyCommentCount(days)) {
            commentMap.put(row.get("date").toString(), ((Number) row.get("count")).longValue());
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        // 从 6 天前到今天，共7个点
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DATE_FMT);
            Map<String, Object> point = new HashMap<>();
            point.put("date", dateStr);
            point.put("postCount", postMap.getOrDefault(dateStr, 0L));
            point.put("commentCount", commentMap.getOrDefault(dateStr, 0L));
            trend.add(point);
        }
        vo.setLast7DaysTrend(trend);

        return vo;
    }

    // ============================================================
    // 8. 待审核帖子数（导航角标）
    // ============================================================
    @Override
    public long getPendingCount() {
        return adminPostMapper.countPendingPosts();
    }

    /**
     * 帖子状态码 → 中文名称（用于饼图label）
     */
    private String statusName(int status) {
        switch (status) {
            case 0: return "草稿";
            case 1: return "审核中";
            case 2: return "已发布";
            case 3: return "已拒绝";
            case 4: return "管理员删除";
            default: return "未知";
        }
    }
}

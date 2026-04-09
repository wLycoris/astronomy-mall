-- =============================================================
-- 7.8 论坛通知模板 INSERT SQL
-- 文件: src/main/resources/sql/7.8_forum_notification_templates.sql
-- 创建日期: 2026-04-08
-- =============================================================
-- 📌 用途:
--   7.7 已入库 2 条 (FORUM_POST_APPROVED / FORUM_POST_REJECTED, id=25/26)
--   7.8 阶段二补齐剩余 7 条论坛通知模板，与 NotificationHelper 7.8 新增的
--   6 个论坛通知方法 + 7.5 已实现的 sendUserFollowedNotification 一一对应。
--
-- 📌 模板 code 命名规则:
--     {MODULE.toUpperCase()}_{TYPE.toUpperCase()}
--   NotificationServiceImpl.sendNotification() 即用此规则匹配。
--
-- 📌 jumpUrl 约定:
--     所有帖子相关跳转统一指向 /forum/list?postId={postId}
--     ForumList.vue onMounted 会读取 ?postId 自动打开详情对话框
--   用户/作者主页跳转: /forum/user/{userId}
--
-- 📌 priority:
--     0 = 普通    (点赞/收藏/评论/被@)
--     1 = 重要    (帖子升为热门)
--
-- 📌 enabled = 1   (默认启用，用户可在「通知设置」单独关闭论坛模块)
--
-- ⚠️ 执行前请确认 7.7 的 25/26 已入库；本文件不重复插入这 2 条。
-- =============================================================

INSERT INTO `tb_notification_template`
  (`code`,`module`,`type`,`title_template`,`content_template`,`jump_url_template`,`variables`,`enabled`,`remark`)
VALUES

-- (1) 帖子被点赞 ─────────────────────────────────────────────
('FORUM_POST_LIKED','forum','post_liked','收到了新点赞 ❤️',
 '{likerNickname} 赞了你的帖子《{postTitle}》',
 '/forum/list?postId={postId}',
 '{"likerNickname":"点赞者昵称","postTitle":"帖子标题","postId":"帖子ID"}',
 1,'帖子被点赞通知（PostServiceImpl.likePost 触发，防自通知）'),

-- (2) 帖子被评论 ─────────────────────────────────────────────
('FORUM_POST_COMMENTED','forum','post_commented','你的帖子收到了新评论 💬',
 '{commenterNickname} 评论了你的帖子《{postTitle}》：{commentSnippet}',
 '/forum/list?postId={postId}',
 '{"commenterNickname":"评论者昵称","postTitle":"帖子标题","commentSnippet":"评论内容片段","postId":"帖子ID"}',
 1,'帖子被评论通知（CommentServiceImpl.addComment 顶级评论触发，防自通知）'),

-- (3) 评论被回复 ─────────────────────────────────────────────
('FORUM_COMMENT_REPLIED','forum','comment_replied','有人回复了你的评论 ↩️',
 '{replierNickname} 回复了你：{replySnippet}',
 '/forum/list?postId={postId}',
 '{"replierNickname":"回复者昵称","replySnippet":"回复内容片段","postId":"帖子ID"}',
 1,'评论被回复通知（CommentServiceImpl.addComment 子回复触发，防自通知）'),

-- (4) 帖子被收藏 ─────────────────────────────────────────────
('FORUM_POST_COLLECTED','forum','post_collected','你的帖子被收藏了 ⭐',
 '{collectorNickname} 收藏了你的帖子《{postTitle}》',
 '/forum/list?postId={postId}',
 '{"collectorNickname":"收藏者昵称","postTitle":"帖子标题","postId":"帖子ID"}',
 1,'帖子被收藏通知（PostServiceImpl.collectPost 触发，防自通知）'),

-- (5) 被@提及 ────────────────────────────────────────────────
('FORUM_MENTIONED','forum','mentioned','有人在论坛 @ 了你 👀',
 '{mentionerNickname} 在帖子里提到了你',
 '/forum/list?postId={postId}',
 '{"mentionerNickname":"@发起人昵称","postId":"帖子ID"}',
 1,'被@提及通知（扩展功能，前端解析@表达式后批量调用）'),

-- (6) 被关注 ─────────────────────────────────────────────────
-- ⚠️ FollowServiceImpl.follow() 在 7.5 已经调用 sendUserFollowedNotification，
--    但当时模板未入库，所以通知一直走不通。本次补齐。
('FORUM_USER_FOLLOWED','forum','user_followed','你有了新粉丝 ✨',
 '{followerNickname} 关注了你，去看看 ta 的主页吧',
 '/forum/user/{followerId}',
 '{"followerNickname":"关注者昵称","followerId":"关注者ID"}',
 1,'被关注通知（FollowServiceImpl.follow 触发，7.5 已写代码 7.8 补模板）'),

-- (7) 帖子升为热门 ───────────────────────────────────────────
('FORUM_POST_TRENDING','forum','post_trending','你的帖子上热门了 🔥',
 '恭喜！你的帖子《{postTitle}》热度持续上升，已被大家热烈关注',
 '/forum/list?postId={postId}',
 '{"postTitle":"帖子标题","postId":"帖子ID"}',
 1,'帖子升为热门通知（ForumScheduler.calcHotScores 检测 is_hot 0→1 时触发，每帖一生只发一次）');

-- =============================================================
-- 验证 SQL（执行后查看）:
--   SELECT id, code, module, type, enabled FROM tb_notification_template
--    WHERE module='forum' ORDER BY id;
--   -- 期望: 共 9 条 (id 25/26 + 新增 7 条)
-- =============================================================

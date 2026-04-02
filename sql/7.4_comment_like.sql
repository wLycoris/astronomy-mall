-- =============================================
-- 7.4 评论点赞表 (tb_comment_like)
-- =============================================
-- 与 tb_post_like 同模式: INSERT=点赞, DELETE=取消点赞
-- 唯一约束 uk_comment_user 防止重复点赞
-- =============================================
CREATE TABLE IF NOT EXISTS `tb_comment_like` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `comment_id`  bigint(20) NOT NULL COMMENT '关联tb_post_comment.id',
  `user_id`     bigint(20) NOT NULL COMMENT '关联tb_user.id',
  `create_time` datetime   DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

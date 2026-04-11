-- =============================================
-- 推荐系统模块 8.0 基础建设 — 建表SQL
-- 执行: mysql -u root -proot -P 3308 astronomy_mall --default-character-set=utf8mb4 < 8.0_recommend_tables.sql
-- =============================================

-- ① tb_browse_log 商品浏览记录表
-- Redis 去重: SETNX "browse:dedup:{userId}:{productId}" TTL=1800s
-- CF 权重: 浏览(1) + 收藏(2) + 购买(3)
-- 定时清理: 每周一凌晨 3 点 DELETE WHERE browse_time < 90 天前
CREATE TABLE IF NOT EXISTS `tb_browse_log` (
  `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
  `user_id`     bigint(20)  NOT NULL COMMENT '📌关联tb_user.id',
  `product_id`  bigint(20)  NOT NULL COMMENT '📌关联tb_product.id',
  `category_id` bigint(20)  DEFAULT NULL COMMENT '分类ID(冗余,加速按类推荐)',
  `source`      varchar(20) DEFAULT 'detail' COMMENT 'detail/list',
  `browse_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_time` (`user_id`, `browse_time`),
  KEY `idx_browse_time` (`browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品浏览记录(推荐系统CF数据源)';

-- ② tb_post_browse_log 帖子浏览记录表
-- Redis 去重: SETNX "post:browse:dedup:{userId}:{postId}" TTL=1800s
-- 定时清理: 与 tb_browse_log 同步每周清理 90 天前
CREATE TABLE IF NOT EXISTS `tb_post_browse_log` (
  `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
  `user_id`     bigint(20)  NOT NULL COMMENT '📌关联tb_user.id',
  `post_id`     bigint(20)  NOT NULL COMMENT '📌关联tb_post.id',
  `browse_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `duration`    int(11)     DEFAULT 0 COMMENT '浏览时长(秒,可选)',
  `source`      varchar(50) DEFAULT NULL COMMENT '来源页面',
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_time` (`user_id`, `browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子浏览记录(推荐系统内容相似度数据源)';

-- ③ tb_recommend_record 推荐曝光与点击记录表
-- 用途: 答辩时现场展示曝光总量 / 点击率，无需做 A/B test
-- 写入: 所有推荐接口返回前异步批量 INSERT
-- 点击: 前端点击推荐卡片 → POST /api/recommend/click 回写 is_clicked + click_time
CREATE TABLE IF NOT EXISTS `tb_recommend_record` (
  `id`              bigint(20)  NOT NULL AUTO_INCREMENT,
  `user_id`         bigint(20)  NOT NULL COMMENT '📌关联tb_user.id',
  `recommend_type`  varchar(50) NOT NULL COMMENT 'product/course/post',
  `target_id`       bigint(20)  NOT NULL COMMENT '被推荐的目标ID',
  `algorithm`       varchar(50) NOT NULL COMMENT 'content/collaborative/hot/coldstart',
  `score`           double      DEFAULT NULL COMMENT '推荐得分(可空)',
  `position`        int(11)     DEFAULT NULL COMMENT '推荐位置(1-10)',
  `is_clicked`      tinyint(4)  DEFAULT 0 COMMENT '0-未点击 1-已点击',
  `click_time`      datetime    DEFAULT NULL,
  `create_time`     datetime    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐曝光与点击记录(答辩数据支撑)';

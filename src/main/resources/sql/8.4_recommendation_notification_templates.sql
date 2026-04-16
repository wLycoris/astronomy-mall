-- =============================================
-- 8.4 推荐系统通知模板 (2 条)
-- =============================================
-- 执行前提: tb_notification_template 已有 33 行 (id 1-33)
-- 执行方式: mysql -u root -p --default-character-set=utf8mb4 astronomy_mall < 8.4_recommendation_notification_templates.sql
--
-- 模板编码规则: {MODULE}_{TYPE} (全大写)
--   module = recommend
--   type   = product_recommend / course_recommend
--
-- 触发时机:
--   RECOMMEND_PRODUCT_RECOMMEND → PriceDropScheduler 检测到降价 + 用户 interest_tags 命中商品标签时
--   RECOMMEND_COURSE_RECOMMEND  → RecognitionPollScheduler 识别成功后推荐 Top1 匹配课程时
-- =============================================

INSERT INTO `tb_notification_template`
    (`id`, `code`, `module`, `type`, `title_template`, `content_template`,
     `jump_url_template`, `variables`, `enabled`, `remark`)
VALUES
(34, 'RECOMMEND_PRODUCT_RECOMMEND', 'recommend', 'product_recommend',
 '为你推荐商品',
 '根据您的兴趣，发现一款可能喜欢的商品：{productName}，当前价格 ¥{price}',
 '/product/{productId}',
 '{"productName":"商品名称","price":"当前价格","productId":"商品ID"}',
 1, '8.4 推荐商品通知 — 降价 + interest_tags 命中时触发'),

(35, 'RECOMMEND_COURSE_RECOMMEND', 'recommend', 'course_recommend',
 '为你推荐课程',
 '根据您的星图识别结果，推荐课程《{courseName}》，快来学习吧',
 '/course/{courseId}',
 '{"courseName":"课程名称","courseId":"课程ID"}',
 1, '8.4 推荐课程通知 — AI识别成功后 Top1 匹配课程');

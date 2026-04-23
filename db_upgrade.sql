USE `gradate_design_db`;

-- 如果 reviews 表还没有 parent_id 字段，执行以下语句：
ALTER TABLE `reviews` ADD COLUMN `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID，若为null则为主打分评论';

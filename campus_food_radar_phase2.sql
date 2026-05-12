-- 校园美食雷达 - 第二阶段：数据库升级 SQL
-- 目标表: users, shops, reviews, review_likes

USE `gradate_design_db`;

-- 1. users 表增加学生认证相关字段
-- verify_status: 0=未认证, 1=待审核, 2=已认证, 3=已驳回
ALTER TABLE `users`
ADD COLUMN `verify_status` TINYINT DEFAULT 0 COMMENT '学生认证状态：0=未认证, 1=待审核, 2=已认证, 3=已驳回' AFTER `role`,
ADD COLUMN `verify_image` VARCHAR(512) DEFAULT NULL COMMENT '学生证件照URL' AFTER `verify_status`;

-- 插入一个演示用的管理员账号
-- 密码均为 123456 的 MD5 (e10adc3949ba59abbe56e057f20f883e)
-- role: 2 代表系统管理员
INSERT IGNORE INTO `users` (`username`, `password`, `nickname`, `role`) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 2);

-- 2. shops 表增加浏览量字段
ALTER TABLE `shops`
ADD COLUMN `view_count` INT DEFAULT 0 COMMENT '浏览量' AFTER `avg_rating`;

-- 3. reviews 表增加多维度评分与点赞数字段
ALTER TABLE `reviews`
ADD COLUMN `likes` INT DEFAULT 0 COMMENT '点赞数' AFTER `rating`,
ADD COLUMN `score_taste` DECIMAL(2,1) DEFAULT NULL COMMENT '口味评分',
ADD COLUMN `score_env` DECIMAL(2,1) DEFAULT NULL COMMENT '环境评分',
ADD COLUMN `score_service` DECIMAL(2,1) DEFAULT NULL COMMENT '服务评分',
ADD COLUMN `score_value` DECIMAL(2,1) DEFAULT NULL COMMENT '性价比评分';

-- 4. 新建 review_likes 表 (用于防止用户重复点赞)
CREATE TABLE IF NOT EXISTS `review_likes` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `review_id` BIGINT NOT NULL COMMENT '评价ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_review_user` (`review_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价点赞记录表';

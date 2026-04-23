USE `gradate_design_db`;

-- ===== 1. users 表新增角色字段 =====
ALTER TABLE `users` ADD COLUMN `role` TINYINT DEFAULT 0 COMMENT '0=普通用户, 1=商家';

-- ===== 2. shops 表新增商家关联和状态 =====
ALTER TABLE `shops` ADD COLUMN `owner_id` BIGINT DEFAULT NULL COMMENT '商家用户ID';
ALTER TABLE `shops` ADD COLUMN `description` VARCHAR(500) DEFAULT '' COMMENT '商铺简介';
ALTER TABLE `shops` ADD COLUMN `status` TINYINT DEFAULT 1 COMMENT '0=下架, 1=正常营业';

-- ===== 3. 新建 shop_stats_daily 每日统计表 =====
CREATE TABLE IF NOT EXISTS `shop_stats_daily` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `shop_id` BIGINT NOT NULL COMMENT '关联店铺ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `new_reviews` INT DEFAULT 0 COMMENT '当日新增评价数',
  `avg_rating` DECIMAL(2,1) DEFAULT 0.0 COMMENT '当日平均评分',
  `total_reviews` INT DEFAULT 0 COMMENT '累计评价总数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_shop_date` (`shop_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商铺每日统计快照';

-- ===== 4. 如果 reviews 表还没有 parent_id，补上 =====
-- ALTER TABLE `reviews` ADD COLUMN `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID';

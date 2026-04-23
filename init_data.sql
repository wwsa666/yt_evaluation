USE `gradate_design_db`;

-- 清理旧数据（如果需要可以取消注释）
-- TRUNCATE TABLE `shop_types`;
-- TRUNCATE TABLE `shops`;

-- 插入商铺类型
INSERT INTO `shop_types` (`id`, `name`, `sort`, `create_time`) VALUES
(1, '美食', 1, NOW()),
(2, '休闲娱乐', 2, NOW()),
(3, '酒店住宿', 3, NOW());

-- 插入一些演示商铺
INSERT INTO `shops` (`type_id`, `name`, `address`, `image`, `avg_rating`, `comments_count`, `create_time`) VALUES
(1, '随便烧烤档', '随便路123号', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=500&q=80', 4.8, 10, NOW()),
(1, '随便咖啡馆', '随便街456号', 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=500&q=80', 4.5, 50, NOW()),
(3, '随便大酒店', '观景路1号', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&q=80', 4.9, 20, NOW()),
(2, '随便网咖', '科技路888号', 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&q=80', 4.2, 80, NOW());

-- 校园美食雷达 Phase 3 数据库升级脚本
-- 执行此脚本前，请确保您在 `gradate_design_db` 数据库中。

USE `gradate_design_db`;

-- 1. 更新 users 表，增加商家申请状态与账号封禁状态
ALTER TABLE `users` 
ADD COLUMN `merchant_status` tinyint(4) DEFAULT 0 COMMENT '商家申请状态：0=未申请，1=审核中，2=已通过，3=已驳回',
ADD COLUMN `status` tinyint(4) DEFAULT 1 COMMENT '账号状态：1=正常，0=封禁';

-- 更新已有商家的 merchant_status 为 2
UPDATE `users` SET `merchant_status` = 2 WHERE `role` = 1;

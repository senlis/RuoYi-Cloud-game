-- ============================================
-- 游戏运营管理平台 - 表结构变更迁移脚本
-- 在已有数据库上执行此脚本（适用于 game_schema.sql 旧版建表后的升级）
-- ============================================

-- 1. game_server 移除冗余字段
ALTER TABLE game_server
  DROP COLUMN channel_id,
  DROP COLUMN project_id;

-- 2. game_region 新增代理分区key和config字段
ALTER TABLE game_region
  ADD COLUMN proxy_region_key varchar(100) DEFAULT NULL COMMENT '代理分区key' AFTER sort,
  ADD COLUMN config text COMMENT '分区导出配置(JSON)' AFTER proxy_region_key;

-- 3. game_region 重命名 export_config 为 servers
ALTER TABLE game_region
  CHANGE COLUMN export_config servers text COMMENT '分区下服务器配置(JSON数组)';

-- 4. game_server 字段重构：server_id→id, server_code→server_id(int)
ALTER TABLE game_server
  CHANGE COLUMN server_id id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID' FIRST,
  CHANGE COLUMN server_code server_id int(11) NOT NULL COMMENT '服务器ID(数字编码)' AFTER id,
  DROP INDEX idx_region,
  ADD UNIQUE KEY uk_server_region (server_id, region_id);

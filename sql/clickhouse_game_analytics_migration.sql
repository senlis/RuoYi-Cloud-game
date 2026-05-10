-- ============================================
-- 游戏数据分析 - ClickHouse 表迁移脚本（v1→v2）
-- 新增字段：role_name / vip / fight
-- ============================================

-- 1. game_event_role_levelup 新增 role_name
ALTER TABLE game_event_role_levelup
    ADD COLUMN IF NOT EXISTS role_name String AFTER role_id;

-- 2. game_event_role_event 新增 role_name、vip、fight
ALTER TABLE game_event_role_event
    ADD COLUMN IF NOT EXISTS role_name String AFTER role_id;
ALTER TABLE game_event_role_event
    ADD COLUMN IF NOT EXISTS vip UInt16 AFTER account_id;
ALTER TABLE game_event_role_event
    ADD COLUMN IF NOT EXISTS fight UInt64 AFTER vip;

-- 3. game_event_item_change 新增 role_name
ALTER TABLE game_event_item_change
    ADD COLUMN IF NOT EXISTS role_name String AFTER role_id;

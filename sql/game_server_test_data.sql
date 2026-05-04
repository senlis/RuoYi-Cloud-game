-- ============================================
-- 游戏服务器测试数据（每个分区5条，服务器ID从100开始）
-- server_type: 0=正式 1=测试 2=体验
-- status: 0=初始 1=待开 2=停服 3=维护 4=正常
-- ============================================

-- 分区1 (region_id=1, iOS官方大区)
INSERT INTO game_server (server_id, region_id, server_name, server_type, status, open_time, backend_url, game_db_config, log_db_config, merge_info, sort, remark, create_by, create_time) VALUES
(100, 1, 'iOS 1服', '0', '4', '2025-01-01 10:00:00',
 'http://192.168.1.101:8080/api',
 '{"host":"192.168.1.101","port":3306,"db":"game_s100","user":"game","pwd":"****"}',
 '{"host":"192.168.1.101","port":3307,"db":"log_s100","user":"game","pwd":"****"}',
 '[{"mergeServer":"s99","mergeTime":"2024-12-20"}]',
 1, 'iOS首发大区，玩家活跃度高', 'admin', sysdate()),

(101, 1, 'iOS 2服', '0', '4', '2025-01-05 10:00:00',
 'http://192.168.1.102:8080/api',
 '{"host":"192.168.1.102","port":3306,"db":"game_s101","user":"game","pwd":"****"}',
 '{"host":"192.168.1.102","port":3307,"db":"log_s101","user":"game","pwd":"****"}',
 null, 2, '人气火爆，排队中', 'admin', sysdate()),

(102, 1, 'iOS 3服', '0', '4', '2025-01-10 10:00:00',
 'http://192.168.1.103:8080/api',
 '{"host":"192.168.1.103","port":3306,"db":"game_s102","user":"game","pwd":"****"}',
 '{"host":"192.168.1.103","port":3307,"db":"log_s102","user":"game","pwd":"****"}',
 null, 3, '', 'admin', sysdate()),

(103, 1, 'iOS 4服', '0', '4', '2025-01-15 10:00:00',
 'http://192.168.1.104:8080/api',
 '{"host":"192.168.1.104","port":3306,"db":"game_s103","user":"game","pwd":"****"}',
 '{"host":"192.168.1.104","port":3307,"db":"log_s103","user":"game","pwd":"****"}',
 null, 4, '', 'admin', sysdate()),

(104, 1, 'iOS 5服', '0', '3', '2025-01-20 10:00:00',
 'http://192.168.1.105:8080/api',
 '{"host":"192.168.1.105","port":3306,"db":"game_s104","user":"game","pwd":"****"}',
 '{"host":"192.168.1.105","port":3307,"db":"log_s104","user":"game","pwd":"****"}',
 null, 5, '例行维护中', 'admin', sysdate());

-- 分区2 (region_id=2, Android大区)
INSERT INTO game_server (server_id, region_id, server_name, server_type, status, open_time, backend_url, game_db_config, log_db_config, merge_info, sort, remark, create_by, create_time) VALUES
(105, 2, 'Android 1服', '0', '4', '2025-02-01 10:00:00',
 'http://192.168.2.101:8080/api',
 '{"host":"192.168.2.101","port":3306,"db":"game_s105","user":"game","pwd":"****"}',
 '{"host":"192.168.2.101","port":3307,"db":"log_s105","user":"game","pwd":"****"}',
 null, 1, 'Android渠道主服', 'admin', sysdate()),

(106, 2, 'Android 2服', '0', '4', '2025-02-05 10:00:00',
 'http://192.168.2.102:8080/api',
 '{"host":"192.168.2.102","port":3306,"db":"game_s106","user":"game","pwd":"****"}',
 '{"host":"192.168.2.102","port":3307,"db":"log_s106","user":"game","pwd":"****"}',
 '[{"mergeServer":"s99","mergeTime":"2025-01-15"}]', 2, '', 'admin', sysdate()),

(107, 2, 'Android 3服', '0', '4', '2025-02-10 10:00:00',
 'http://192.168.2.103:8080/api',
 '{"host":"192.168.2.103","port":3306,"db":"game_s107","user":"game","pwd":"****"}',
 '{"host":"192.168.2.103","port":3307,"db":"log_s107","user":"game","pwd":"****"}',
 null, 3, '', 'admin', sysdate()),

(108, 2, 'Android 4服', '0', '4', '2025-02-15 10:00:00',
 'http://192.168.2.104:8080/api',
 '{"host":"192.168.2.104","port":3306,"db":"game_s108","user":"game","pwd":"****"}',
 '{"host":"192.168.2.104","port":3307,"db":"log_s108","user":"game","pwd":"****"}',
 null, 4, '', 'admin', sysdate()),

(109, 2, 'Android 5服', '0', '3', '2025-02-20 10:00:00',
 'http://192.168.2.105:8080/api',
 '{"host":"192.168.2.105","port":3306,"db":"game_s109","user":"game","pwd":"****"}',
 '{"host":"192.168.2.105","port":3307,"db":"log_s109","user":"game","pwd":"****"}',
 null, 5, '合服准备中', 'admin', sysdate());

-- 分区3 (region_id=3, 官方大区)
INSERT INTO game_server (server_id, region_id, server_name, server_type, status, open_time, backend_url, game_db_config, log_db_config, merge_info, sort, remark, create_by, create_time) VALUES
(110, 3, '官方 1服', '0', '4', '2025-03-01 10:00:00',
 'http://192.168.3.101:8080/api',
 '{"host":"192.168.3.101","port":3306,"db":"game_s110","user":"game","pwd":"****"}',
 '{"host":"192.168.3.101","port":3307,"db":"log_s110","user":"game","pwd":"****"}',
 null, 1, '官网渠道主服', 'admin', sysdate()),

(111, 3, '官方 2服', '0', '4', '2025-03-05 10:00:00',
 'http://192.168.3.102:8080/api',
 '{"host":"192.168.3.102","port":3306,"db":"game_s111","user":"game","pwd":"****"}',
 '{"host":"192.168.3.102","port":3307,"db":"log_s111","user":"game","pwd":"****"}',
 null, 2, '', 'admin', sysdate()),

(112, 3, '官方 3服', '0', '4', '2025-03-10 10:00:00',
 'http://192.168.3.103:8080/api',
 '{"host":"192.168.3.103","port":3306,"db":"game_s112","user":"game","pwd":"****"}',
 '{"host":"192.168.3.103","port":3307,"db":"log_s112","user":"game","pwd":"****"}',
 null, 3, '', 'admin', sysdate()),

(113, 3, '官方 4服', '0', '4', '2025-03-15 10:00:00',
 'http://192.168.3.104:8080/api',
 '{"host":"192.168.3.104","port":3306,"db":"game_s113","user":"game","pwd":"****"}',
 '{"host":"192.168.3.104","port":3307,"db":"log_s113","user":"game","pwd":"****"}',
 null, 4, '', 'admin', sysdate()),

(114, 3, '官方 5服', '0', '4', '2025-03-20 10:00:00',
 'http://192.168.3.105:8080/api',
 '{"host":"192.168.3.105","port":3306,"db":"game_s114","user":"game","pwd":"****"}',
 '{"host":"192.168.3.105","port":3307,"db":"log_s114","user":"game","pwd":"****"}',
 null, 5, '', 'admin', sysdate());

-- 分区4 (region_id=4, 体验大区)
INSERT INTO game_server (server_id, region_id, server_name, server_type, status, open_time, backend_url, game_db_config, log_db_config, merge_info, sort, remark, create_by, create_time) VALUES
(115, 4, '体验 1服', '2', '4', '2025-04-01 10:00:00',
 'http://192.168.4.101:8080/api',
 '{"host":"192.168.4.101","port":3306,"db":"game_s115","user":"game","pwd":"****"}',
 '{"host":"192.168.4.101","port":3307,"db":"log_s115","user":"game","pwd":"****"}',
 null, 1, '新功能体验服', 'admin', sysdate()),

(116, 4, '体验 2服', '2', '4', '2025-04-05 10:00:00',
 'http://192.168.4.102:8080/api',
 '{"host":"192.168.4.102","port":3306,"db":"game_s116","user":"game","pwd":"****"}',
 '{"host":"192.168.4.102","port":3307,"db":"log_s116","user":"game","pwd":"****"}',
 null, 2, '压测专用', 'admin', sysdate()),

(117, 4, '体验 3服', '2', '4', '2025-04-10 10:00:00',
 'http://192.168.4.103:8080/api',
 '{"host":"192.168.4.103","port":3306,"db":"game_s117","user":"game","pwd":"****"}',
 '{"host":"192.168.4.103","port":3307,"db":"log_s117","user":"game","pwd":"****"}',
 null, 3, '', 'admin', sysdate()),

(118, 4, '体验 4服', '2', '4', '2025-04-15 10:00:00',
 'http://192.168.4.104:8080/api',
 '{"host":"192.168.4.104","port":3306,"db":"game_s118","user":"game","pwd":"****"}',
 '{"host":"192.168.4.104","port":3307,"db":"log_s118","user":"game","pwd":"****"}',
 null, 4, '', 'admin', sysdate()),

(119, 4, '体验 5服', '2', '4', '2025-04-20 10:00:00',
 'http://192.168.4.105:8080/api',
 '{"host":"192.168.4.105","port":3306,"db":"game_s119","user":"game","pwd":"****"}',
 '{"host":"192.168.4.105","port":3307,"db":"log_s119","user":"game","pwd":"****"}',
 null, 5, '', 'admin', sysdate());

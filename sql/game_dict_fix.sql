-- ============================================
-- 游戏字典修复脚本
-- 解决 dict_id(10) 冲突问题
-- 先清理可能已存在的冲突数据，再重新插入
-- ============================================

-- 1. 清理旧的冲突记录（如果之前插入成功了一部分）
DELETE FROM sys_dict_data WHERE dict_type IN ('game_channel_type', 'game_server_type', 'game_server_status', 'game_entity_type', 'game_field_type');
DELETE FROM sys_dict_type  WHERE dict_type IN ('game_channel_type', 'game_server_type', 'game_server_status', 'game_entity_type', 'game_field_type');

-- 2. 重新插入字典类型（dict_id 从 100 开始，避免与原有 1-10 冲突）
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(100, '渠道类型',   'game_channel_type',   '0', 'admin', sysdate(), '游戏渠道类型'),
(101, '服务器类型',   'game_server_type',    '0', 'admin', sysdate(), '游戏服务器类型'),
(102, '服务器状态',   'game_server_status',  '0', 'admin', sysdate(), '游戏服务器运行状态'),
(103, '实体类型',     'game_entity_type',    '0', 'admin', sysdate(), '动态字段所属实体类型'),
(104, '字段类型',     'game_field_type',     '0', 'admin', sysdate(), '动态字段数据类型');

-- 3. 重新插入字典数据
-- game_channel_type
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(100, 1, '官方', '0', 'game_channel_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '官方自营渠道'),
(101, 2, '联运', '1', 'game_channel_type', '', 'success', 'N', '0', 'admin', sysdate(), '联运合作渠道'),
(102, 3, '海外', '2', 'game_channel_type', '', 'warning', 'N', '0', 'admin', sysdate(), '海外发行渠道');

-- game_server_type
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(110, 1, '正式', '0', 'game_server_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '正式运营服务器'),
(111, 2, '测试', '1', 'game_server_type', '', 'warning', 'N', '0', 'admin', sysdate(), '测试服务器'),
(112, 3, '体验', '2', 'game_server_type', '', 'info', 'N', '0', 'admin', sysdate(), '体验服务器');

-- game_server_status
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(120, 1, '初始', '0', 'game_server_status', '', 'info', 'N', '0', 'admin', sysdate(), '服务器初始状态'),
(121, 2, '待开', '1', 'game_server_status', '', 'warning', 'N', '0', 'admin', sysdate(), '等待开服'),
(122, 3, '停服', '2', 'game_server_status', '', 'danger', 'N', '0', 'admin', sysdate(), '服务器已停服'),
(123, 4, '维护', '3', 'game_server_status', '', 'warning', 'N', '0', 'admin', sysdate(), '服务器维护中'),
(124, 5, '正常', '4', 'game_server_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '服务器运行正常');

-- game_entity_type
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(130, 1, '项目', 'project', 'game_entity_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '游戏项目'),
(131, 2, '渠道', 'channel', 'game_entity_type', '', 'success', 'N', '0', 'admin', sysdate(), '推广渠道'),
(132, 3, '分区', 'region', 'game_entity_type', '', 'warning', 'N', '0', 'admin', sysdate(), '游戏大区'),
(133, 4, '服务器', 'server', 'game_entity_type', '', 'info', 'N', '0', 'admin', sysdate(), '游戏服务器');

-- game_field_type
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(140, 1, '文本', 'text', 'game_field_type', '', '', 'Y', '0', 'admin', sysdate(), '单行文本输入'),
(141, 2, '数值', 'number', 'game_field_type', '', '', 'N', '0', 'admin', sysdate(), '数字输入'),
(142, 3, '多行文本', 'textarea', 'game_field_type', '', '', 'N', '0', 'admin', sysdate(), '多行文本输入'),
(143, 4, '日期', 'date', 'game_field_type', '', '', 'N', '0', 'admin', sysdate(), '日期时间选择'),
(144, 5, '下拉选择', 'select', 'game_field_type', '', '', 'N', '0', 'admin', sysdate(), '下拉选项选择'),
(145, 6, '开关', 'boolean', 'game_field_type', '', '', 'N', '0', 'admin', sysdate(), '开关(Y/N)');

-- ============================================
-- 游戏运营管理平台 - 菜单与字典初始化脚本
-- 先执行 game_schema.sql 建表，再执行本脚本
-- ============================================

-- ============================================
-- 1. 字典类型
-- ============================================
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(100, '渠道类型', 'game_channel_type', '0', 'admin', sysdate(), '游戏渠道类型'),
(101, '服务器类型', 'game_server_type', '0', 'admin', sysdate(), '游戏服务器类型'),
(102, '服务器状态', 'game_server_status', '0', 'admin', sysdate(), '游戏服务器运行状态'),
(103, '实体类型', 'game_entity_type', '0', 'admin', sysdate(), '动态字段所属实体类型'),
(104, '字段类型', 'game_field_type', '0', 'admin', sysdate(), '动态字段数据类型');

-- ============================================
-- 2. 字典数据
-- ============================================

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

-- ============================================
-- 3. 菜单
-- ============================================

-- 3.1 顶级菜单：游戏管理
INSERT INTO sys_menu VALUES('2000', '游戏管理', '0', '5', 'game',     null, '', '', 1, 0, 'M', '0', '0', '',                        'table',           'admin', sysdate(), '', null, '游戏管理菜单');

-- 3.2 子菜单：项目管理
INSERT INTO sys_menu VALUES('2100', '项目管理',   '2000', '1', 'project',    'game/project',    '', '', 1, 0, 'C', '0', '0', 'game:project:list',   'guide',           'admin', sysdate(), '', null, '项目管理菜单');
INSERT INTO sys_menu VALUES('2101', '项目查询',   '2100', '1', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:project:query',  '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2102', '项目新增',   '2100', '2', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:project:add',    '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2103', '项目修改',   '2100', '3', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:project:edit',   '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2104', '项目删除',   '2100', '4', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:project:remove', '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2105', '项目导出',   '2100', '5', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:project:export', '#',               'admin', sysdate(), '', null, '');

-- 3.3 子菜单：渠道管理
INSERT INTO sys_menu VALUES('2200', '渠道管理',   '2000', '2', 'channel',    'game/channel',    '', '', 1, 0, 'C', '0', '0', 'game:channel:list',   'link',            'admin', sysdate(), '', null, '渠道管理菜单');
INSERT INTO sys_menu VALUES('2201', '渠道查询',   '2200', '1', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:channel:query',  '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2202', '渠道新增',   '2200', '2', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:channel:add',    '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2203', '渠道修改',   '2200', '3', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:channel:edit',   '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2204', '渠道删除',   '2200', '4', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:channel:remove', '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2205', '渠道导出',   '2200', '5', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:channel:export', '#',               'admin', sysdate(), '', null, '');

-- 3.4 子菜单：分区管理
INSERT INTO sys_menu VALUES('2300', '分区管理',   '2000', '3', 'region',     'game/region',     '', '', 1, 0, 'C', '0', '0', 'game:region:list',    'tree-table',      'admin', sysdate(), '', null, '分区管理菜单');
INSERT INTO sys_menu VALUES('2301', '分区查询',   '2300', '1', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:region:query',   '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2302', '分区新增',   '2300', '2', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:region:add',     '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2303', '分区修改',   '2300', '3', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:region:edit',    '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2304', '分区删除',   '2300', '4', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:region:remove',  '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2305', '分区导出',   '2300', '5', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:region:export',  '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2306', '导出配置',   '2300', '6', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:region:exportConfig', '#',         'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2307', '克隆分区',   '2300', '7', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:region:clone',   '#',               'admin', sysdate(), '', null, '');

-- 3.5 子菜单：服务器管理
INSERT INTO sys_menu VALUES('2400', '服务器管理', '2000', '4', 'server',     'game/server',     '', '', 1, 0, 'C', '0', '0', 'game:server:list',   'server',          'admin', sysdate(), '', null, '服务器管理菜单');
INSERT INTO sys_menu VALUES('2401', '服务器查询', '2400', '1', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:server:query',  '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2402', '服务器新增', '2400', '2', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:server:add',    '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2403', '服务器修改', '2400', '3', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:server:edit',   '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2404', '服务器删除', '2400', '4', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:server:remove', '#',               'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2405', '服务器导出', '2400', '5', '',           '',                '', '', 1, 0, 'F', '0', '0', 'game:server:export', '#',               'admin', sysdate(), '', null, '');

-- 3.6 系统管理下：动态字段配置
INSERT INTO sys_menu VALUES('2500', '动态字段配置', '1', '10', 'fieldDefine', 'game/fieldDefine', '', '', 1, 0, 'C', '0', '0', 'game:field:list', 'color', 'admin', sysdate(), '', null, '动态字段配置菜单');
INSERT INTO sys_menu VALUES('2501', '字段查询',   '2500', '1', '',           '',                  '', '', 1, 0, 'F', '0', '0', 'game:field:query',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2502', '字段新增',   '2500', '2', '',           '',                  '', '', 1, 0, 'F', '0', '0', 'game:field:add',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2503', '字段修改',   '2500', '3', '',           '',                  '', '', 1, 0, 'F', '0', '0', 'game:field:edit',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2504', '字段删除',   '2500', '4', '',           '',                  '', '', 1, 0, 'F', '0', '0', 'game:field:remove', '#', 'admin', sysdate(), '', null, '');

-- 3.7 系统管理下：游戏角色授权
INSERT INTO sys_menu VALUES('2600', '游戏角色授权', '1', '11', 'gameRoleAuth', 'system/gameRoleAuth', '', '', 1, 0, 'C', '0', '0', 'game:auth:list', 'auth', 'admin', sysdate(), '', null, '游戏角色授权菜单');
INSERT INTO sys_menu VALUES('2601', '授权查询',     '2600', '1', '',            '',                    '', '', 1, 0, 'F', '0', '0', 'game:auth:list',  '#',    'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2602', '授权修改',     '2600', '2', '',            '',                    '', '', 1, 0, 'F', '0', '0', 'game:auth:edit',  '#',    'admin', sysdate(), '', null, '');

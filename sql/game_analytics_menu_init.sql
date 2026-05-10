-- ============================================
-- 游戏数据分析 - 菜单初始化脚本 v2
-- ============================================

-- 顶级菜单：数据分析（挂在游戏管理 2000 下）
INSERT INTO sys_menu VALUES('2700', '数据分析', '2000', '6', 'analytics', null, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', null, '数据分析菜单');

-- 每日总览 (is_cache=1)
INSERT INTO sys_menu VALUES('2701', '每日总览',     '2700', '1', 'overview',   'game/analytics/overview',   '', '', 1, 1, 'C', '0', '0', 'game:analytics:list',     'chart', 'admin', sysdate(), '', null, '每日总览菜单');
INSERT INTO sys_menu VALUES('2702', '留存分析',     '2700', '2', 'retention',  'game/analytics/retention',  '', '', 1, 1, 'C', '0', '0', 'game:analytics:list',     'trend', 'admin', sysdate(), '', null, '留存分析菜单');
INSERT INTO sys_menu VALUES('2703', 'LTV分析',      '2700', '3', 'ltv',        'game/analytics/ltv',        '', '', 1, 1, 'C', '0', '0', 'game:analytics:list',     'money', 'admin', sysdate(), '', null, 'LTV分析菜单');
INSERT INTO sys_menu VALUES('2704', '充值分析',     '2700', '4', 'recharge',   'game/analytics/recharge',   '', '', 1, 1, 'C', '0', '0', 'game:analytics:list',     'pay',   'admin', sysdate(), '', null, '充值分析菜单');
INSERT INTO sys_menu VALUES('2705', '等级分析',     '2700', '5', 'level',      'game/analytics/level',      '', '', 1, 1, 'C', '0', '0', 'game:analytics:list',     'list',  'admin', sysdate(), '', null, '等级分析菜单');
INSERT INTO sys_menu VALUES('2706', '道具经济',     '2700', '6', 'item',       'game/analytics/item',       '', '', 1, 1, 'C', '0', '0', 'game:analytics:list',     'box',   'admin', sysdate(), '', null, '道具经济菜单');

-- 玩家行为日志 (is_cache=1)
INSERT INTO sys_menu VALUES('2710', '玩家行为日志', '2700', '7', 'eventLog',   'game/analytics/eventLog',   '', '', 1, 1, 'C', '0', '0', 'game:analytics:eventLog',  'log',   'admin', sysdate(), '', null, '玩家行为日志菜单');
INSERT INTO sys_menu VALUES('2711', '行为日志查询', '2710', '1', '',           '',                          '', '', 1, 0, 'F', '0', '0', 'game:analytics:eventLog',  '#',     'admin', sysdate(), '', null, '');

-- 事件类型配置
INSERT INTO sys_menu VALUES('2720', '事件类型配置', '2700', '8', 'eventType',  'game/analytics/eventType',  '', '', 1, 1, 'C', '0', '0', 'game:analytics:eventType', 'edit',   'admin', sysdate(), '', null, '事件类型配置菜单');
INSERT INTO sys_menu VALUES('2721', '事件类型查询', '2720', '1', '',           '',                          '', '', 1, 0, 'F', '0', '0', 'game:analytics:eventType', '#',      'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2722', '事件类型新增', '2720', '2', '',           '',                          '', '', 1, 0, 'F', '0', '0', 'game:analytics:eventType', '#',      'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2723', '事件类型修改', '2720', '3', '',           '',                          '', '', 1, 0, 'F', '0', '0', 'game:analytics:eventType', '#',      'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES('2724', '事件类型删除', '2720', '4', '',           '',                          '', '', 1, 0, 'F', '0', '0', 'game:analytics:eventType', '#',      'admin', sysdate(), '', null, '');

-- ============================================
-- 游戏角色权限授权表
-- ============================================
CREATE TABLE IF NOT EXISTS game_role_auth (
  auth_id     bigint(20) NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  role_id     bigint(20) NOT NULL COMMENT '角色ID',
  entity_type varchar(50) NOT NULL COMMENT '实体类型(project/channel/region)',
  entity_id   bigint(20) NOT NULL COMMENT '实体ID',
  PRIMARY KEY (auth_id),
  UNIQUE KEY uk_role_entity (role_id, entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏角色权限授权表';

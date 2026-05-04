package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 游戏角色权限对象 game_role_auth
 *
 * @author ruoyi
 */
public class GameRoleAuth
{
    private static final long serialVersionUID = 1L;

    /** 权限ID */
    private Long authId;

    /** 角色ID */
    private Long roleId;

    /** 实体类型(project, channel, region) */
    private String entityType;

    /** 实体ID */
    private Long entityId;

    public Long getAuthId()
    {
        return authId;
    }

    public void setAuthId(Long authId)
    {
        this.authId = authId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }

    public Long getEntityId()
    {
        return entityId;
    }

    public void setEntityId(Long entityId)
    {
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("authId", getAuthId())
            .append("roleId", getRoleId())
            .append("entityType", getEntityType())
            .append("entityId", getEntityId())
            .toString();
    }
}

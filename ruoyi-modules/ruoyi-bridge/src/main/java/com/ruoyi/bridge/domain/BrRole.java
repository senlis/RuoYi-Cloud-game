package com.ruoyi.bridge.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 角色信息表 br_role
 * <p>
 * 按渠道分库存储，每个渠道库独立建表。
 * 记录游戏角色的基本信息（等级、职业、VIP、战斗力等）。
 *
 * @author ruoyi
 */
public class BrRole {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long roleId;

    /** 渠道KEY */
    private String channelKey;

    /** 分区CODE */
    private String regionCode;

    /** 服务器ID */
    private Integer serverId;

    /** 账号ID */
    private String accountId;

    /** 游戏服角色ID */
    private Long extRoleId;

    /** 角色名 */
    private String roleName;

    /** 等级 */
    private Integer level;

    /** 职业 */
    private Integer vocation;

    /** VIP等级 */
    private Integer vip;

    /** 战斗力 */
    private Long fight;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roleId", getRoleId())
                .append("channelKey", getChannelKey())
                .append("regionCode", getRegionCode())
                .append("serverId", getServerId())
                .append("accountId", getAccountId())
                .append("extRoleId", getExtRoleId())
                .append("roleName", getRoleName())
                .append("level", getLevel())
                .append("vocation", getVocation())
                .append("vip", getVip())
                .append("fight", getFight())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Long getExtRoleId() {
        return extRoleId;
    }

    public void setExtRoleId(Long extRoleId) {
        this.extRoleId = extRoleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getVocation() {
        return vocation;
    }

    public void setVocation(Integer vocation) {
        this.vocation = vocation;
    }

    public Integer getVip() {
        return vip;
    }

    public void setVip(Integer vip) {
        this.vip = vip;
    }

    public Long getFight() {
        return fight;
    }

    public void setFight(Long fight) {
        this.fight = fight;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

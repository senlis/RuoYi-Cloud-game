package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
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

    /** 角色ID */
    @Excel(name = "角色ID")
    private Long playerId;

    /** 渠道KEY */
    @Excel(name = "渠道KEY")
    private String channelKey;

    /** 分区CODE */
    private String regionCode;

    /** 服务器ID */
    @Excel(name = "服务器ID")
    private Integer serverId;

    /** 服务器ID列表（多选查询用，逗号分隔） */
    private String serverIds;

    /** 账号ID */
    @Excel(name = "账号ID")
    private String accountId;

    /** 角色名 */
    @Excel(name = "角色名")
    private String roleName;

    /** 等级 */
    @Excel(name = "等级")
    private Integer level;

    /** 职业 */
    @Excel(name = "职业")
    private Integer vocation;

    /** VIP等级 */
    @Excel(name = "VIP等级")
    private Integer vip;

    /** 战斗力 */
    @Excel(name = "战斗力")
    private Long fight;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("playerId", getPlayerId())
                .append("channelKey", getChannelKey())
                .append("regionCode", getRegionCode())
                .append("serverId", getServerId())
                .append("serverIds", getServerIds())
                .append("accountId", getAccountId())
                .append("roleName", getRoleName())
                .append("level", getLevel())
                .append("vocation", getVocation())
                .append("vip", getVip())
                .append("fight", getFight())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
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

    public String getServerIds() {
        return serverIds;
    }

    public void setServerIds(String serverIds) {
        this.serverIds = serverIds;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

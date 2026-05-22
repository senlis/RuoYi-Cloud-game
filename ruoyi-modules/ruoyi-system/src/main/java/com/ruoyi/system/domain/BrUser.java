package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 渠道用户表 br_user
 * <p>
 * 按渠道分库存储，每个渠道库独立建表。
 * 记录渠道用户的基本信息。
 *
 * @author ruoyi
 */
public class BrUser {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 平台唯一标识 */
    @Excel(name = "平台标识")
    private String identityId;

    /** 渠道标识 */
    @Excel(name = "渠道KEY")
    private String channelKey;

    /** 渠道用户ID */
    @Excel(name = "渠道用户ID")
    private String channelUserId;

    /** 服务器ID */
    @Excel(name = "服务器ID")
    private Integer serverId;

    /** 服务器ID列表（多选查询用，逗号分隔） */
    private String serverIds;

    /** 游戏角色ID */
    @Excel(name = "角色ID")
    private Long playerId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("identityId", getIdentityId())
                .append("channelKey", getChannelKey())
                .append("channelUserId", getChannelUserId())
                .append("serverId", getServerId())
                .append("serverIds", getServerIds())
                .append("playerId", getPlayerId())
                .append("createTime", getCreateTime())
                .append("lastLoginTime", getLastLoginTime())
                .toString();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getChannelUserId() {
        return channelUserId;
    }

    public void setChannelUserId(String channelUserId) {
        this.channelUserId = channelUserId;
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

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}

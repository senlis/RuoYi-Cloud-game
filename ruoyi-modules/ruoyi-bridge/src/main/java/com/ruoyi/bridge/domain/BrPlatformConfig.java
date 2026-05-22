package com.ruoyi.bridge.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 渠道接入平台配置表 br_platform_config
 * <p>
 * 每个渠道可独立配置数据库连接信息，支持按渠道分库存储。
 * channel_key 唯一标识一个渠道平台。
 *
 * @author ruoyi
 */
public class BrPlatformConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 平台ID（自增主键） */
    private Long platformId;

    /** 渠道KEY */
    private String channelKey;

    /** 平台名称 */
    private String platformName;

    /** 平台状态（0正常 1停用） */
    private String status;

    /** 充值状态（0正常 1停用） */
    private String rechargeStatus;

    /** 数据库地址 */
    private String dbHost;

    /** 数据库端口 */
    private Integer dbPort;

    /** 数据库名 */
    private String dbName;

    /** 数据库用户 */
    private String dbUser;

    /** 数据库密码 */
    private String dbPwd;

    /** 备注 */
    private String remark;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("platformId", getPlatformId())
                .append("channelKey", getChannelKey())
                .append("platformName", getPlatformName())
                .append("status", getStatus())
                .append("rechargeStatus", getRechargeStatus())
                .append("dbHost", getDbHost())
                .append("dbPort", getDbPort())
                .append("dbName", getDbName())
                .append("dbUser", getDbUser())
                .append("dbPwd", getDbPwd())
                .append("remark", getRemark())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }

    public Long getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Long platformId) {
        this.platformId = platformId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRechargeStatus() {
        return rechargeStatus;
    }

    public void setRechargeStatus(String rechargeStatus) {
        this.rechargeStatus = rechargeStatus;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public Integer getDbPort() {
        return dbPort;
    }

    public void setDbPort(Integer dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public void setRemark(String remark) {
        this.remark = remark;
    }
}

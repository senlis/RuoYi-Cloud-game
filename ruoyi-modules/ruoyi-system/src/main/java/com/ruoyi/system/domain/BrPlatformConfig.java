package com.ruoyi.system.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 渠道接入平台配置对象 br_platform_config
 *
 * @author ruoyi
 */
public class BrPlatformConfig extends BaseEntity {

    private Long platformId;
    private String channelKey;
    private String platformName;
    private String status;
    private String rechargeStatus;
    private String dbHost;
    private Integer dbPort;
    private String dbName;
    private String dbUser;
    private String dbPwd;

    public Long getPlatformId() { return platformId; }
    public void setPlatformId(Long platformId) { this.platformId = platformId; }
    public String getChannelKey() { return channelKey; }
    public void setChannelKey(String channelKey) { this.channelKey = channelKey; }
    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRechargeStatus() { return rechargeStatus; }
    public void setRechargeStatus(String rechargeStatus) { this.rechargeStatus = rechargeStatus; }
    public String getDbHost() { return dbHost; }
    public void setDbHost(String dbHost) { this.dbHost = dbHost; }
    public Integer getDbPort() { return dbPort; }
    public void setDbPort(Integer dbPort) { this.dbPort = dbPort; }
    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }
    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }
    public String getDbPwd() { return dbPwd; }
    public void setDbPwd(String dbPwd) { this.dbPwd = dbPwd; }
}

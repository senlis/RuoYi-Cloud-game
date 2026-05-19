package com.ruoyi.system.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

/**
 * 渠道参数配置对象 br_channel_arg_config
 *
 * @author ruoyi
 */
public class BrChannelArgConfig extends BaseEntity {

    private Long channelId;
    @NotBlank
    private String channelKey;
    @NotBlank
    private String regionKey;
    private String platformName;
    private String packageName;
    private String appId;
    private String appKey;
    private String payKey;
    private String authUrl;
    private String packageParams;
    private String status;
    private Integer sort;

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getChannelKey() { return channelKey; }
    public void setChannelKey(String channelKey) { this.channelKey = channelKey; }
    public String getRegionKey() { return regionKey; }
    public void setRegionKey(String regionKey) { this.regionKey = regionKey; }
    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getPayKey() { return payKey; }
    public void setPayKey(String payKey) { this.payKey = payKey; }
    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }
    public String getPackageParams() { return packageParams; }
    public void setPackageParams(String packageParams) { this.packageParams = packageParams; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}

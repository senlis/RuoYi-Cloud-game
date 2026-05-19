package com.ruoyi.bridge.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 渠道出包参数配置表 br_channel_arg_config
 * <p>
 * channel_key + region_key 联合唯一定位一组出包参数。
 *
 * @author ruoyi
 */
public class BrChannelArgConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 渠道ID（自增主键） */
    private Long channelId;

    /** 渠道标识（来自 game_channel.channel_code） */
    @NotBlank(message = "渠道标识不能为空")
    @Size(max = 64, message = "渠道标识长度不能超过64个字符")
    private String channelKey;

    /** 区域标识（来自 game_region.region_code） */
    @NotBlank(message = "区域标识不能为空")
    @Size(max = 128, message = "区域标识长度不能超过128个字符")
    private String regionKey;

    /** 平台名称 */
    @Size(max = 64, message = "平台名称长度不能超过64个字符")
    private String platformName;

    /** 包名 */
    @Size(max = 64, message = "包名长度不能超过64个字符")
    private String packageName;

    /** 应用ID */
    @Size(max = 256, message = "应用ID长度不能超过256个字符")
    private String appId;

    /** 应用密钥 */
    private String appKey;

    /** 支付密钥 */
    private String payKey;

    /** 认证地址 */
    @Size(max = 512, message = "认证地址长度不能超过512个字符")
    private String authUrl;

    /** 出包参数(JSON) */
    private String packageParams;

    /** 状态（0正常 1停用） */
    private String status;

    /** 显示顺序 */
    private Integer sort;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("channelId", getChannelId())
                .append("channelKey", getChannelKey())
                .append("regionKey", getRegionKey())
                .append("platformName", getPlatformName())
                .append("packageName", getPackageName())
                .append("appId", getAppId())
                .append("appKey", getAppKey())
                .append("payKey", getPayKey())
                .append("authUrl", getAuthUrl())
                .append("packageParams", getPackageParams())
                .append("sort", getSort())
                .append("status", getStatus())
                .append("remark", getRemark())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getRegionKey() {
        return regionKey;
    }

    public void setRegionKey(String regionKey) {
        this.regionKey = regionKey;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getPayKey() {
        return payKey;
    }

    public void setPayKey(String payKey) {
        this.payKey = payKey;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getPackageParams() {
        return packageParams;
    }

    public void setPackageParams(String packageParams) {
        this.packageParams = packageParams;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}

package com.ruoyi.bridge.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 渠道配置表 br_channel_config
 * <p>
 * channel_key 唯一定位一组出包参数（渠道+发行平台）
 * 例如: huawei_cn, huawei_overseas, oppo_cn
 *
 * @author ruoyi
 */
public class BrChannelConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 渠道ID */
    private Long channelId;

    /** 渠道唯一标识（如 huawei_cn） */
    private String channelKey;

    /** 渠道名称 */
    private String channelName;

    /** 基础渠道（如 huawei） */
    private String channelBase;

    /** 发行平台（如 cn/overseas/hk） */
    private String publishPlatform;

    /** 应用ID */
    private String appId;

    /** 应用密钥 */
    private String appKey;

    /** 合作方ID */
    private String cpId;

    /** 合作方AppID */
    private String cpAppId;

    /** 支付公钥 */
    private String payPublicKey;

    /** 支付私钥 */
    private String payPrivateKey;

    /** SDK认证地址 */
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
                .append("channelName", getChannelName())
                .append("channelBase", getChannelBase())
                .append("publishPlatform", getPublishPlatform())
                .append("appId", getAppId())
                .append("appKey", getAppKey())
                .append("cpId", getCpId())
                .append("cpAppId", getCpAppId())
                .append("payPublicKey", getPayPublicKey())
                .append("payPrivateKey", getPayPrivateKey())
                .append("authUrl", getAuthUrl())
                .append("packageParams", getPackageParams())
                .append("sort", getSort())
                .append("status", getStatus())
                .append("remark", getRemark())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getChannelKey() { return channelKey; }
    public void setChannelKey(String channelKey) { this.channelKey = channelKey; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getChannelBase() { return channelBase; }
    public void setChannelBase(String channelBase) { this.channelBase = channelBase; }
    public String getPublishPlatform() { return publishPlatform; }
    public void setPublishPlatform(String publishPlatform) { this.publishPlatform = publishPlatform; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getCpId() { return cpId; }
    public void setCpId(String cpId) { this.cpId = cpId; }
    public String getCpAppId() { return cpAppId; }
    public void setCpAppId(String cpAppId) { this.cpAppId = cpAppId; }
    public String getPayPublicKey() { return payPublicKey; }
    public void setPayPublicKey(String payPublicKey) { this.payPublicKey = payPublicKey; }
    public String getPayPrivateKey() { return payPrivateKey; }
    public void setPayPrivateKey(String payPrivateKey) { this.payPrivateKey = payPrivateKey; }
    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }
    public String getPackageParams() { return packageParams; }
    public void setPackageParams(String packageParams) { this.packageParams = packageParams; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

package com.ruoyi.bridge.domain.dto;

import java.util.Map;

/**
 * 渠道认证请求DTO
 *
 * @author ruoyi
 */
public class AuthRequest {

    /** 渠道标识 */
    private String channelKey;

    /** 包ID */
    private String packageId;

    /** 服务器ID */
    private Integer serverId;

    /** 用户ID（渠道用户ID） */
    private String userId;

    /** 签名 */
    private String sign;

    /** 令牌 */
    private String token;

    /** 扩展参数 */
    private Map<String, String> extra;

    /** 时间戳 */
    private Long timestamp;

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

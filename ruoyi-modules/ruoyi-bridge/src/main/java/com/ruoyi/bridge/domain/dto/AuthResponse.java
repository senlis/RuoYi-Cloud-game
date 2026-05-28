package com.ruoyi.bridge.domain.dto;

import lombok.Data;

/**
 * 渠道认证响应DTO
 *
 * @author ruoyi
 */
@Data
public class AuthResponse {

    /** 平台唯一标识 */
    private String identityId;

    /** 用户ID */
    private String userId;

    /** 服务器ID */
    private Integer serverId;

    /** 渠道标识 */
    private String channelKey;

    /** 签名 */
    private String sign;

    /** 时间戳 */
    private Long timestamp;

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

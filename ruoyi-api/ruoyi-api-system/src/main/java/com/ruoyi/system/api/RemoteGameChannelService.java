package com.ruoyi.system.api;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteGameChannelFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 游戏渠道服务 Feign 客户端
 * <p>
 * 供 auth 模块远程调用 system 模块查询渠道配置和更新 SecureKey。
 *
 * @author ruoyi
 */
@FeignClient(contextId = "remoteGameChannelService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteGameChannelFallbackFactory.class)
public interface RemoteGameChannelService {

    /**
     * 查询渠道 SecureKey 相关信息（内部调用）
     * <p>
     * 返回 channel_id, channel_code(appId), project_id, clickhouse_config
     *
     * @param channelId 渠道 ID
     * @param source    内部调用标识
     */
    @GetMapping("/game/channel/secure-info/{channelId}")
    R<Map<String, Object>> getChannelSecureInfo(@PathVariable("channelId") Long channelId,
                                                 @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 更新渠道 SecureKey 字段（内部调用）
     *
     * @param channelId 渠道 ID
     * @param data      secure_key_hash, secure_key_salt, secure_key_version, secure_key_updated_at
     * @param source    内部调用标识
     */
    @PutMapping("/game/channel/secure-key/{channelId}")
    R<Void> updateChannelSecureKey(@PathVariable("channelId") Long channelId,
                                    @RequestBody Map<String, Object> data,
                                    @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}

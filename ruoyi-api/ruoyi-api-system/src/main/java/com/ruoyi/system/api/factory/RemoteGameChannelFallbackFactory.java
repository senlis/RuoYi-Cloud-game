package com.ruoyi.system.api.factory;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteGameChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 游戏渠道服务降级处理
 *
 * @author ruoyi
 */
@Component
public class RemoteGameChannelFallbackFactory implements FallbackFactory<RemoteGameChannelService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteGameChannelFallbackFactory.class);

    @Override
    public RemoteGameChannelService create(Throwable throwable) {
        log.error("游戏渠道服务调用失败:{}", throwable.getMessage());
        return new RemoteGameChannelService() {
            @Override
            public R<Map<String, Object>> getChannelSecureInfo(Long channelId, String source) {
                return R.fail("获取渠道信息失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> updateChannelSecureKey(Long channelId, Map<String, Object> data, String source) {
                return R.fail("更新渠道 SecureKey 失败:" + throwable.getMessage());
            }
        };
    }
}

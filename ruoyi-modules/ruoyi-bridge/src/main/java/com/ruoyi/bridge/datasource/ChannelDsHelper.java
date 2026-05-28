package com.ruoyi.bridge.datasource;

import com.ruoyi.bridge.domain.BrPlatformConfig;
import com.ruoyi.bridge.service.IBrPlatformConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * 渠道数据源切换辅助工具
 * <p>
 * 当 {@link ChannelDs} 注解不适用（如需要在切面前后执行主库操作）时，
 * 使用此工具手动切换数据源。
 * <p>
 * 用法：
 * <pre>
 * String result = channelDsHelper.execute(channelKey, () -> {
 *     // 此处的代码在渠道库中执行
 *     return brUserMapper.selectByUserKey(...);
 * });
 * </pre>
 *
 * @author ruoyi
 */
@Component
public class ChannelDsHelper {

    private static final Logger log = LoggerFactory.getLogger(ChannelDsHelper.class);

    @Autowired
    private IBrPlatformConfigService platformConfigService;

    @Autowired
    private ChannelRoutingDataSource routingDataSource;

    /**
     * 在指定渠道数据源上下文中执行操作
     *
     * @param channelKey 渠道KEY
     * @param callback   回调（在渠道库中执行）
     * @param <T>        返回值类型
     * @return 回调返回结果
     */
    public <T> T execute(String channelKey, Callable<T> callback) {
        BrPlatformConfig config = platformConfigService.selectByChannelKey(channelKey);
        if (config == null) {
            throw new RuntimeException("渠道 " + channelKey + " 未配置平台信息");
        }
        if ("1".equals(config.getStatus())) {
            throw new RuntimeException("渠道 " + channelKey + " 已停用");
        }

        try {
            if (hasDbConfig(config)) {
                routingDataSource.register(channelKey, config);
            }
            routingDataSource.push(channelKey);
            try {
                return callback.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new RuntimeException("渠道数据源操作失败: " + e.getMessage(), e);
            }
        } finally {
            routingDataSource.poll();
        }
    }

    /**
     * 在指定渠道数据源上下文中执行操作（无返回值）
     */
    public void execute(String channelKey, Runnable runnable) {
        execute(channelKey, () -> {
            runnable.run();
            return null;
        });
    }

    private boolean hasDbConfig(BrPlatformConfig config) {
        return config.getDbHost() != null && !config.getDbHost().isEmpty();
    }
}

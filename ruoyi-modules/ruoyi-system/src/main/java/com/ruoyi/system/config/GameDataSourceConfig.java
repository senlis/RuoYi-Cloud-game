package com.ruoyi.system.config;

import com.ruoyi.system.helper.GameRoutingDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 游戏数据源配置
 * 使用 GameRoutingDataSource 包装主库，支持动态注册游戏库/日志库数据源
 *
 * @author ruoyi
 */
@Configuration
public class GameDataSourceConfig
{
    @Value("${spring.datasource.dynamic.datasource.master.url}")
    private String url;

    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String password;

    @Bean
    @Primary
    public GameRoutingDataSource gameRoutingDataSource()
    {
        return new GameRoutingDataSource(url, username, password);
    }
}

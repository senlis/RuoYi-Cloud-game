package com.ruoyi.bridge.config;

import com.ruoyi.bridge.datasource.ChannelRoutingDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 数据源配置
 * <p>
 * 将 ChannelRoutingDataSource 设为主数据源，支持按渠道动态切换。
 * 主库连接信息直接读取 Nacos 配置，避免循环依赖。
 *
 * @author ruoyi
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.dynamic.datasource.master.url:jdbc:mysql://localhost:3306/ry-cloud}")
    private String url;

    @Value("${spring.datasource.dynamic.datasource.master.username:root}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.master.password:123456}")
    private String password;

    @Primary
    @Bean
    public ChannelRoutingDataSource channelRoutingDataSource() {
        ChannelRoutingDataSource routingDataSource = new ChannelRoutingDataSource();
        routingDataSource.initDefaultDataSource(url, username, password);
        return routingDataSource;
    }
}

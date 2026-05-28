package com.ruoyi.bridge.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.ruoyi.bridge.domain.BrPlatformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 渠道动态路由数据源
 * <p>
 * 继承 AbstractRoutingDataSource，根据 ChannelDsContext 中设置的 channelKey
 * 动态切换到对应的渠道独立数据库。如果渠道有自己的独立数据库配置，则创建对应的 DruidDataSource；
 * 否则使用默认的主数据源。
 * <p>
 * 工作原理：
 * <ol>
 *   <li>determineCurrentLookupKey() 从 ChannelDsContext 获取当前 channelKey</li>
 *   <li>若 channelKey 对应的 DataSource 已注册，直接返回</li>
 *   <li>若未注册但渠道有独立 DB 配置，动态创建并注册</li>
 *   <li>若 channelKey 为 null，回退到主数据源（master）</li>
 * </ol>
 *
 * @author ruoyi
 */
public class ChannelRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(ChannelRoutingDataSource.class);

    /** 主数据源标识 */
    public static final String MASTER = "master";

    /** 渠道专属数据源缓存 Map<channelKey, DataSource> */
    private final Map<String, DataSource> channelDataSources = new ConcurrentHashMap<>();

    /** 默认主数据源 */
    private DataSource defaultDataSource;

    /**
     * 初始化默认主数据源（读取 Nacos 连接信息，自行创建 Druid 连接池，避免循环依赖）
     *
     * @param url      数据库URL
     * @param username 数据库用户名
     * @param password 数据库密码
     */
    public void initDefaultDataSource(String url, String username, String password) {
        DruidDataSource ds = new DruidDataSource();
        ds.setName("master");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setInitialSize(5);
        ds.setMinIdle(5);
        ds.setMaxActive(20);
        ds.setMaxWait(60000L);
        ds.setValidationQuery("SELECT 1");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        try {
            ds.init();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("主数据源初始化失败", e);
        }
        this.defaultDataSource = ds;
        super.setDefaultTargetDataSource(ds);
        Map<Object, Object> targets = new ConcurrentHashMap<>();
        targets.put(MASTER, ds);
        super.setTargetDataSources(targets);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String channelKey = ChannelDsContext.get();
        if (channelKey == null) {
            return MASTER;
        }
        // 如果该渠道的数据源已注册，直接路由
        if (channelDataSources.containsKey(channelKey)) {
            return channelKey;
        }
        // 未注册则走主数据源
        return MASTER;
    }

    /**
     * 切换到指定渠道的数据源
     *
     * @param channelKey 渠道KEY
     */
    public void push(String channelKey) {
        ChannelDsContext.set(channelKey);
    }

    /**
     * 重置回主数据源并清除上下文
     */
    public void poll() {
        ChannelDsContext.clear();
    }

    /**
     * 根据平台配置注册渠道专属数据源
     *
     * @param channelKey 渠道KEY
     * @param config    渠道平台配置（含数据库连接信息）
     */
    public synchronized void register(String channelKey, BrPlatformConfig config) {
        if (channelDataSources.containsKey(channelKey)) {
            log.debug("渠道 {} 的数据源已存在，跳过注册", channelKey);
            return;
        }

        if (config == null || config.getDbHost() == null || config.getDbHost().isEmpty()) {
            log.warn("渠道 {} 未配置独立数据库，使用主数据源", channelKey);
            return;
        }

        String url = "jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort() + "/"
                + config.getDbName() + "?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";

        DruidDataSource ds = new DruidDataSource();
        ds.setName("channel-" + channelKey);
        ds.setUrl(url);
        ds.setUsername(config.getDbUser());
        ds.setPassword(config.getDbPwd());
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 连接池配置
        ds.setInitialSize(5);
        ds.setMinIdle(5);
        ds.setMaxActive(20);
        ds.setMaxWait(60000);
        ds.setTimeBetweenEvictionRunsMillis(60000);
        ds.setMinEvictableIdleTimeMillis(300000);
        ds.setValidationQuery("SELECT 1");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);

        try {
            ds.init();
            channelDataSources.put(channelKey, ds);

            // 将新数据源注册到目标数据源映射中
            Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();
            targetDataSources.put(MASTER, defaultDataSource);
            targetDataSources.putAll(channelDataSources);
            super.setTargetDataSources(targetDataSources);
            super.afterPropertiesSet();

            log.info("渠道 {} 数据源注册成功: {}", channelKey, url);
        } catch (Exception e) {
            log.error("渠道 {} 数据源初始化失败: {}", channelKey, e.getMessage(), e);
            throw new RuntimeException("渠道数据源初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 移除指定渠道的数据源
     *
     * @param channelKey 渠道KEY
     */
    public synchronized void remove(String channelKey) {
        DataSource ds = channelDataSources.remove(channelKey);
        if (ds != null) {
            if (ds instanceof DruidDataSource) {
                ((DruidDataSource) ds).close();
            }
            // 更新目标数据源映射
            Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();
            targetDataSources.put(MASTER, defaultDataSource);
            targetDataSources.putAll(channelDataSources);
            super.setTargetDataSources(targetDataSources);
            super.afterPropertiesSet();
            log.info("渠道 {} 数据源已移除", channelKey);
        }
    }

    /**
     * 获取已注册的渠道数据源数量
     *
     * @return 数据源数量
     */
    public int getChannelDataSourceCount() {
        return channelDataSources.size();
    }
}

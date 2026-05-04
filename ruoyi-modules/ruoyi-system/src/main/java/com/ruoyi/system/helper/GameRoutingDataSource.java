package com.ruoyi.system.helper;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏路由数据源
 * 默认使用主库，可通过线程变量切换到游戏库/日志库
 *
 * @author yinxinglin
 */
public class GameRoutingDataSource extends AbstractRoutingDataSource
{
    private static final Logger log = LoggerFactory.getLogger(GameRoutingDataSource.class);

    private static final ThreadLocal<String> DS_HOLDER = new ThreadLocal<>();

    private final Map<String, DataSource> gameDataSources = new ConcurrentHashMap<>();

    private final DataSource masterDataSource;

    public GameRoutingDataSource(String url, String username, String password)
    {
        this.masterDataSource = createMasterDataSource(url, username, password);
        super.setDefaultTargetDataSource(masterDataSource);
        Map<Object, Object> targets = new ConcurrentHashMap<>();
        targets.put("master", masterDataSource);
        super.setTargetDataSources(targets);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey()
    {
        String key = DS_HOLDER.get();
        return key != null ? key : "master";
    }

    @Override
    protected DataSource determineTargetDataSource()
    {
        String key = DS_HOLDER.get();
        if (key != null && gameDataSources.containsKey(key))
        {
            DataSource ds = gameDataSources.get(key);
            try (Connection conn = ds.getConnection())
            {
                if (conn.isValid(3))
                {
                    return ds;
                }
            }
            catch (SQLException e)
            {
                log.warn("数据源 {} 连接失效，尝试重建", key);
                gameDataSources.remove(key);
            }
        }
        return masterDataSource;
    }

    public static void push(String dsName)
    {
        DS_HOLDER.set(dsName);
    }

    public static void poll()
    {
        DS_HOLDER.remove();
    }

    public String getOrCreateGameDb(String regionId, String serverId, String dbConfigJson)
    {
        String dsName = "game_" + regionId + "_" + serverId;
        return getOrCreate(dsName, dbConfigJson);
    }

    public String getOrCreateLogDb(String regionId, String serverId, String dbConfigJson)
    {
        String dsName = "log_" + regionId + "_" + serverId;
        return getOrCreate(dsName, dbConfigJson);
    }

    private String getOrCreate(String dsName, String dbConfigJson)
    {
        if (!gameDataSources.containsKey(dsName))
        {
            gameDataSources.put(dsName, createGameDataSource(dbConfigJson, dsName));
        }
        return dsName;
    }

    public void remove(String dsName)
    {
        DataSource ds = gameDataSources.remove(dsName);
        if (ds instanceof DruidDataSource)
        {
            ((DruidDataSource) ds).close();
            log.info("已关闭并移除动态数据源: {}", dsName);
        }
    }

    private DataSource createMasterDataSource(String url, String username, String password)
    {
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
        ds.setValidationQuery("SELECT 1 FROM DUAL");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        ds.setPoolPreparedStatements(true);
        ds.setMaxPoolPreparedStatementPerConnectionSize(20);
        try
        {
            ds.init();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("主库数据源初始化失败", e);
        }
        return ds;
    }

    private DataSource createGameDataSource(String dbConfigJson, String dsName)
    {
        JSONObject config = JSON.parseObject(dbConfigJson);
        DruidDataSource ds = new DruidDataSource();
        ds.setName(dsName);
        ds.setUrl("jdbc:mysql://" + config.getString("host") + ":" + config.getInteger("port")
                + "/" + config.getString("db")
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai");
        ds.setUsername(config.getString("user"));
        ds.setPassword(config.getString("pwd"));
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setInitialSize(1);
        ds.setMinIdle(1);
        ds.setMaxActive(5);
        ds.setMaxWait(5000L);
        try
        {
            ds.init();
            log.info("已创建动态数据源: {}", dsName);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("数据源初始化失败: " + dsName, e);
        }
        return ds;
    }
}

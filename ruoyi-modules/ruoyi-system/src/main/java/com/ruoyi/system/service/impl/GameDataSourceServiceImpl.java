package com.ruoyi.system.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.GameServer;
import com.ruoyi.system.helper.GameRoutingDataSource;
import com.ruoyi.system.mapper.GameServerMapper;
import com.ruoyi.system.service.IGameDataSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 游戏数据源管理服务实现
 *
 * @author ruoyi
 */
@Service
public class GameDataSourceServiceImpl implements IGameDataSourceService
{
    private static final Logger log = LoggerFactory.getLogger(GameDataSourceServiceImpl.class);

    @Autowired
    private GameRoutingDataSource gameRoutingDataSource;

    @Autowired
    private GameServerMapper gameServerMapper;

    @Override
    public String switchToGameDb(Long regionId, Integer serverId)
    {
        GameServer server = gameServerMapper.selectGameServerByRegionAndId(regionId, serverId);
        if (server == null || server.getGameDbConfig() == null)
        {
            throw new ServiceException("服务器不存在或未配置游戏数据库");
        }
        String dsName = gameRoutingDataSource.getOrCreateGameDb(
                String.valueOf(regionId), String.valueOf(serverId), server.getGameDbConfig());
        GameRoutingDataSource.push(dsName);
        return dsName;
    }

    @Override
    public String switchToLogDb(Long regionId, Integer serverId)
    {
        GameServer server = gameServerMapper.selectGameServerByRegionAndId(regionId, serverId);
        if (server == null || server.getLogDbConfig() == null)
        {
            throw new ServiceException("服务器不存在或未配置日志数据库");
        }
        String dsName = gameRoutingDataSource.getOrCreateLogDb(
                String.valueOf(regionId), String.valueOf(serverId), server.getLogDbConfig());
        GameRoutingDataSource.push(dsName);
        return dsName;
    }

    @Override
    public void switchToMaster()
    {
        GameRoutingDataSource.poll();
    }

    @Override
    public void refreshGameDb(Long regionId, Integer serverId)
    {
        gameRoutingDataSource.remove("game_db_" + regionId + "_" + serverId);
    }

    @Override
    public void refreshLogDb(Long regionId, Integer serverId)
    {
        gameRoutingDataSource.remove("log_db_" + regionId + "_" + serverId);
    }

    @Override
    public void refreshAll(Long regionId, Integer serverId)
    {
        refreshGameDb(regionId, serverId);
        refreshLogDb(regionId, serverId);
    }
}

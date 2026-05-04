package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.GameServer;
import com.ruoyi.system.mapper.GameServerMapper;
import com.ruoyi.system.service.IGameServerService;

/**
 * 游戏服务器 服务层实现
 *
 * @author ruoyi
 */
@Service
public class GameServerServiceImpl implements IGameServerService
{
    @Autowired
    private GameServerMapper gameServerMapper;

    /**
     * 查询游戏服务器
     *
     * @param id 服务器ID
     * @return 游戏服务器
     */
    @Override
    public GameServer selectGameServerById(Long id)
    {
        return gameServerMapper.selectGameServerById(id);
    }

    /**
     * 查询游戏服务器列表
     *
     * @param server 游戏服务器信息
     * @return 游戏服务器集合
     */
    @Override
    public List<GameServer> selectGameServerList(GameServer server)
    {
        return gameServerMapper.selectGameServerList(server);
    }

    /**
     * 根据分区ID查询游戏服务器列表
     *
     * @param regionId 分区ID
     * @return 游戏服务器集合
     */
    @Override
    public List<GameServer> selectGameServerByRegionId(Long regionId)
    {
        return gameServerMapper.selectGameServerByRegionId(regionId);
    }

    /**
     * 新增游戏服务器
     *
     * @param server 游戏服务器信息
     * @return 结果
     */
    @Override
    public int insertGameServer(GameServer server)
    {
        return gameServerMapper.insertGameServer(server);
    }

    /**
     * 修改游戏服务器
     *
     * @param server 游戏服务器信息
     * @return 结果
     */
    @Override
    public int updateGameServer(GameServer server)
    {
        return gameServerMapper.updateGameServer(server);
    }

    /**
     * 批量删除游戏服务器
     *
     * @param serverIds 需要删除的服务器ID
     * @return 结果
     */
    @Override
    public int deleteGameServerByIds(Long[] serverIds)
    {
        return gameServerMapper.deleteGameServerByIds(serverIds);
    }

    /**
     * 校验服务器编码是否唯一
     *
     * @param server 游戏服务器信息
     * @return true=唯一, false=不唯一
     */
    @Override
    public boolean checkServerCodeUnique(GameServer server)
    {
        Long id = server.getId() == null ? -1L : server.getId();
        GameServer info = gameServerMapper.selectGameServerByCode(server.getServerId());
        if (info != null && !info.getId().equals(id))
        {
            return false;
        }
        return true;
    }
}

package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.GameServer;

/**
 * 游戏服务器 数据层
 *
 * @author ruoyi
 */
public interface GameServerMapper
{
    /**
     * 查询游戏服务器列表
     *
     * @param server 游戏服务器信息
     * @return 游戏服务器集合
     */
    public List<GameServer> selectGameServerList(GameServer server);

    /**
     * 根据ID查询游戏服务器
     *
     * @param id 服务器ID
     * @return 游戏服务器
     */
    public GameServer selectGameServerById(Long id);

    /**
     * 根据分区ID查询游戏服务器列表
     *
     * @param regionId 分区ID
     * @return 游戏服务器集合
     */
    public List<GameServer> selectGameServerByRegionId(Long regionId);

    /**
     * 根据服务器ID查询游戏服务器
     *
     * @param serverId 服务器ID
     * @return 游戏服务器
     */
    public GameServer selectGameServerByCode(Integer serverId);

    /**
     * 根据分区ID和服务器ID查询游戏服务器（用于数据源切换）
     *
     * @param regionId 分区ID
     * @param serverId 服务器ID
     * @return 游戏服务器
     */
    public GameServer selectGameServerByRegionAndId(Long regionId, Integer serverId);

    /**
     * 根据服务器ID列表查询游戏服务器
     *
     * @param serverIds 服务器ID列表
     * @return 游戏服务器集合
     */
    public List<GameServer> selectGameServerByCodes(List<Integer> serverIds);

    /**
     * 新增游戏服务器
     *
     * @param server 游戏服务器信息
     * @return 结果
     */
    public int insertGameServer(GameServer server);

    /**
     * 修改游戏服务器
     *
     * @param server 游戏服务器信息
     * @return 结果
     */
    public int updateGameServer(GameServer server);

    /**
     * 删除游戏服务器
     *
     * @param id 服务器ID
     * @return 结果
     */
    public int deleteGameServerById(Long id);

    /**
     * 批量删除游戏服务器
     *
     * @param ids 需要删除的服务器ID
     * @return 结果
     */
    public int deleteGameServerByIds(Long[] ids);
}

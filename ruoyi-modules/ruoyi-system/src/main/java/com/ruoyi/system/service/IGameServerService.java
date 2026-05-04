package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.GameServer;

/**
 * 游戏服务器 服务层
 *
 * @author ruoyi
 */
public interface IGameServerService
{
    /**
     * 查询游戏服务器
     *
     * @param id 服务器ID
     * @return 游戏服务器
     */
    public GameServer selectGameServerById(Long id);

    /**
     * 查询游戏服务器列表
     *
     * @param server 游戏服务器信息
     * @return 游戏服务器集合
     */
    public List<GameServer> selectGameServerList(GameServer server);

    /**
     * 根据分区ID查询游戏服务器列表
     *
     * @param regionId 分区ID
     * @return 游戏服务器集合
     */
    public List<GameServer> selectGameServerByRegionId(Long regionId);

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
     * 批量删除游戏服务器
     *
     * @param serverIds 需要删除的服务器ID
     * @return 结果
     */
    public int deleteGameServerByIds(Long[] serverIds);

    /**
     * 校验服务器编码是否唯一
     *
     * @param server 游戏服务器信息
     * @return true=唯一, false=不唯一
     */
    public boolean checkServerCodeUnique(GameServer server);
}

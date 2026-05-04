package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.system.domain.GameFieldDefine;
import com.ruoyi.system.domain.GameRegion;
import com.ruoyi.system.domain.GameServer;
import com.ruoyi.system.helper.GameAuthContext;
import com.ruoyi.system.mapper.GameRegionMapper;
import com.ruoyi.system.mapper.GameServerMapper;
import com.ruoyi.system.service.IGameFieldDefineService;
import com.ruoyi.system.service.IGameRegionService;
import com.ruoyi.system.service.IGameServerService;

/**
 * 游戏分区 服务层实现
 *
 * @author ruoyi
 */
@Service
public class GameRegionServiceImpl implements IGameRegionService
{
    @Autowired
    private GameRegionMapper gameRegionMapper;

    @Autowired
    private GameServerMapper gameServerMapper;

    @Autowired
    private IGameFieldDefineService gameFieldDefineService;

    @Autowired
    private IGameServerService gameServerService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GameAuthContext gameAuthContext;

    /**
     * 查询游戏分区
     *
     * @param regionId 分区ID
     * @return 游戏分区
     */
    @Override
    public GameRegion selectGameRegionById(Long regionId)
    {
        return gameRegionMapper.selectGameRegionById(regionId);
    }

    /**
     * 查询游戏分区列表
     *
     * @param region 游戏分区信息
     * @return 游戏分区集合
     */
    @Override
    public List<GameRegion> selectGameRegionList(GameRegion region)
    {
        // 非管理员用户，根据角色权限过滤分区列表
        if (!gameAuthContext.isAdmin())
        {
            List<Long> authIds = gameAuthContext.getAuthRegionIds();
            if (authIds != null && !authIds.isEmpty())
            {
                region.getParams().put("authRegionIds", authIds);
            }
            else if (authIds != null && authIds.isEmpty())
            {
                // 有角色但无权限，传入-1使查询结果为空
                region.getParams().put("authRegionIds", java.util.Collections.singletonList(-1L));
            }
        }
        return gameRegionMapper.selectGameRegionList(region);
    }

    /**
     * 根据渠道ID查询游戏分区列表
     *
     * @param channelId 渠道ID
     * @return 游戏分区集合
     */
    @Override
    public List<GameRegion> selectGameRegionByChannelId(Long channelId)
    {
        // 先查询该渠道下所有可见分区
        GameRegion query = new GameRegion();
        query.setChannelId(channelId);

        // 非管理员用户，根据角色权限过滤分区列表
        if (!gameAuthContext.isAdmin())
        {
            List<Long> authIds = gameAuthContext.getAuthRegionIds();
            if (authIds != null && !authIds.isEmpty())
            {
                query.getParams().put("authRegionIds", authIds);
            }
            else if (authIds != null && authIds.isEmpty())
            {
                query.getParams().put("authRegionIds", java.util.Collections.singletonList(-1L));
            }
        }
        return gameRegionMapper.selectGameRegionList(query);
    }

    /**
     * 新增游戏分区
     *
     * @param region 游戏分区信息
     * @return 结果
     */
    @Override
    public int insertGameRegion(GameRegion region)
    {
        return gameRegionMapper.insertGameRegion(region);
    }

    /**
     * 修改游戏分区
     *
     * @param region 游戏分区信息
     * @return 结果
     */
    @Override
    public int updateGameRegion(GameRegion region)
    {
        return gameRegionMapper.updateGameRegion(region);
    }

    /**
     * 批量删除游戏分区
     *
     * @param regionIds 需要删除的分区ID
     * @return 结果
     */
    @Override
    public int deleteGameRegionByIds(Long[] regionIds)
    {
        return gameRegionMapper.deleteGameRegionByIds(regionIds);
    }

    /**
     * 校验分区编码是否唯一
     *
     * @param region 游戏分区信息
     * @return true=唯一, false=不唯一
     */
    @Override
    public boolean checkRegionCodeUnique(GameRegion region)
    {
        Long regionId = region.getRegionId() == null ? -1L : region.getRegionId();
        GameRegion info = gameRegionMapper.selectGameRegionByCode(region.getRegionCode());
        if (info != null && !info.getRegionId().equals(regionId))
        {
            return false;
        }
        return true;
    }

    /**
     * 生成并存储导出配置JSON（拆分为config和servers两部分）
     *
     * @param regionId 分区ID
     * @return 配置JSON字符串（为保持兼容仍返回完整JSON）
     */
    @Override
    public String exportConfig(Long regionId)
    {
        GameRegion region = gameRegionMapper.selectGameRegionById(regionId);

        // 如果设置了代理分区key，则查找被代理的分区并使用其配置
        String proxyKey = region.getProxyRegionKey();
        if (proxyKey != null && !proxyKey.isEmpty())
        {
            GameRegion proxiedRegion = gameRegionMapper.selectGameRegionByKey(proxyKey);
            if (proxiedRegion != null)
            {
                region = proxiedRegion;
                regionId = proxiedRegion.getRegionId();
            }
        }

        List<GameFieldDefine> regionFields = gameFieldDefineService.selectActiveFieldByEntity("region");
        List<GameServer> servers = gameServerMapper.selectGameServerByRegionId(regionId);
        List<GameFieldDefine> serverFields = gameFieldDefineService.selectActiveFieldByEntity("server");

        // -- region config部分 --
        JSONObject configJson = new JSONObject();

        // region fixed fields — flat at top level
        configJson.put("region_id", region.getRegionId());
        configJson.put("region_code", region.getRegionCode());
        configJson.put("region_name", region.getRegionName());
        configJson.put("status", region.getStatus());
        configJson.put("sort", region.getSort());

        // region dynamic fields (only is_export='Y') — flat at top level
        String regionDynamicStr = region.getDynamicFields();
        if (regionDynamicStr != null && !regionDynamicStr.isEmpty())
        {
            JSONObject regionDynamicJson = JSON.parseObject(regionDynamicStr);
            for (GameFieldDefine field : regionFields)
            {
                if ("Y".equals(field.getIsExport()) && regionDynamicJson.containsKey(field.getFieldCode()))
                {
                    configJson.put(field.getFieldCode(), regionDynamicJson.get(field.getFieldCode()));
                }
            }
        }

        String configStr = configJson.toJSONString();

        // -- servers部分 --
        java.util.List<JSONObject> serverList = new java.util.ArrayList<>();
        if (servers != null)
        {
            for (GameServer server : servers)
            {
                JSONObject serverObj = new JSONObject();

                // server fixed fields — flat in server object (不含backend_url, game_db_config, log_db_config)
                serverObj.put("id", server.getId());
                serverObj.put("server_id", server.getServerId());
                serverObj.put("server_name", server.getServerName());
                serverObj.put("server_type", server.getServerType());
                serverObj.put("status", server.getStatus());
                serverObj.put("open_time", server.getOpenTime());
                if (server.getMergeInfo() != null)
                {
                    serverObj.put("merge_info", server.getMergeInfo());
                }
                if (server.getSort() != null)
                {
                    serverObj.put("sort", server.getSort());
                }

                // server dynamic fields (only is_export='Y') — flat in server object
                String serverDynamicStr = server.getDynamicFields();
                if (serverDynamicStr != null && !serverDynamicStr.isEmpty())
                {
                    JSONObject serverDynamicJson = JSON.parseObject(serverDynamicStr);
                    for (GameFieldDefine field : serverFields)
                    {
                        if ("Y".equals(field.getIsExport()) && serverDynamicJson.containsKey(field.getFieldCode()))
                        {
                            serverObj.put(field.getFieldCode(), serverDynamicJson.get(field.getFieldCode()));
                        }
                    }
                }

                serverList.add(serverObj);
            }
        }
        String serversStr = JSON.toJSONString(serverList);

        // 分别存储到DB
        GameRegion updateRegion = new GameRegion();
        updateRegion.setRegionId(regionId);
        updateRegion.setConfig(configStr);
        updateRegion.setServers(serversStr);
        gameRegionMapper.updateGameRegion(updateRegion);

        // 分别缓存到Redis
        redisService.setCacheObject("game:region:config:" + regionId, configStr, 300L, TimeUnit.SECONDS);
        redisService.setCacheObject("game:region:servers:" + regionId, serversStr, 300L, TimeUnit.SECONDS);

        // 返回完整JSON保持兼容
        JSONObject fullJson = new JSONObject();
        fullJson.put("config", JSON.parseObject(configStr));
        fullJson.put("servers", JSON.parseArray(serversStr));
        return fullJson.toJSONString();
    }

    /**
     * 获取分区配置(从缓存或DB)
     *
     * @param regionId 分区ID
     * @return 配置JSON字符串
     */
    @Override
    public String getRegionConfig(Long regionId)
    {
        // try Redis cache first
        String config = redisService.getCacheObject("game:region:config:" + regionId);
        if (config != null)
        {
            return config;
        }

        // read from DB and populate cache
        GameRegion region = gameRegionMapper.selectGameRegionById(regionId);
        if (region != null && region.getConfig() != null)
        {
            redisService.setCacheObject("game:region:config:" + regionId, region.getConfig(), 300L, TimeUnit.SECONDS);
            return region.getConfig();
        }

        return null;
    }

    /**
     * 获取分区服务器列表(从缓存或DB)
     *
     * @param regionId 分区ID
     * @return 服务器JSON数组字符串
     */
    @Override
    public String getRegionServers(Long regionId)
    {
        // try Redis cache first
        String servers = redisService.getCacheObject("game:region:servers:" + regionId);
        if (servers != null)
        {
            return servers;
        }

        // read from DB and populate cache
        GameRegion region = gameRegionMapper.selectGameRegionById(regionId);
        if (region != null && region.getServers() != null)
        {
            redisService.setCacheObject("game:region:servers:" + regionId, region.getServers(), 300L, TimeUnit.SECONDS);
            return region.getServers();
        }

        return null;
    }

    /**
     * 克隆分区
     *
     * @param regionId 源分区ID
     * @param newName 新分区名称
     * @param newCode 新分区编码
     * @param targetChannelId 目标渠道ID
     * @param cloneServers 是否克隆服务器
     * @return 新分区ID
     */
    @Override
    public Long cloneRegion(Long regionId, String newName, String newCode, Long targetChannelId, boolean cloneServers)
    {
        GameRegion source = gameRegionMapper.selectGameRegionById(regionId);

        GameRegion newRegion = new GameRegion();
        newRegion.setRegionCode(newCode);
        newRegion.setRegionName(newName);
        newRegion.setChannelId(targetChannelId != null ? targetChannelId : source.getChannelId());
        newRegion.setProjectId(source.getProjectId());
        newRegion.setStatus(source.getStatus());
        newRegion.setSort(source.getSort());
        newRegion.setDynamicFields(source.getDynamicFields());
        newRegion.setRemark(source.getRemark());

        gameRegionMapper.insertGameRegion(newRegion);
        Long newRegionId = newRegion.getRegionId();

        if (cloneServers)
        {
            List<GameServer> servers = gameServerMapper.selectGameServerByRegionId(regionId);
            for (GameServer server : servers)
            {
                GameServer newServer = new GameServer();
                newServer.setRegionId(newRegionId);
                newServer.setServerId(server.getServerId());
                newServer.setServerType(server.getServerType());
                newServer.setStatus(server.getStatus());
                newServer.setOpenTime(server.getOpenTime());
                newServer.setBackendUrl(server.getBackendUrl());
                newServer.setGameDbConfig(server.getGameDbConfig());
                newServer.setLogDbConfig(server.getLogDbConfig());
                newServer.setMergeInfo(server.getMergeInfo());
                newServer.setSort(server.getSort());
                newServer.setDynamicFields(server.getDynamicFields());
                newServer.setRemark(server.getRemark());
                gameServerMapper.insertGameServer(newServer);
            }
        }

        return newRegionId;
    }
}

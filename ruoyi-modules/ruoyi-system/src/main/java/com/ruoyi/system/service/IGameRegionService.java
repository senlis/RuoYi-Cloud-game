package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.GameRegion;

/**
 * 游戏分区 服务层
 *
 * @author ruoyi
 */
public interface IGameRegionService
{
    /**
     * 查询游戏分区
     *
     * @param regionId 分区ID
     * @return 游戏分区
     */
    public GameRegion selectGameRegionById(Long regionId);

    /**
     * 查询游戏分区列表
     *
     * @param region 游戏分区信息
     * @return 游戏分区集合
     */
    public List<GameRegion> selectGameRegionList(GameRegion region);

    /**
     * 根据渠道ID查询游戏分区列表
     *
     * @param channelId 渠道ID
     * @return 游戏分区集合
     */
    public List<GameRegion> selectGameRegionByChannelId(Long channelId);

    /**
     * 新增游戏分区
     *
     * @param region 游戏分区信息
     * @return 结果
     */
    public int insertGameRegion(GameRegion region);

    /**
     * 修改游戏分区
     *
     * @param region 游戏分区信息
     * @return 结果
     */
    public int updateGameRegion(GameRegion region);

    /**
     * 批量删除游戏分区
     *
     * @param regionIds 需要删除的分区ID
     * @return 结果
     */
    public int deleteGameRegionByIds(Long[] regionIds);

    /**
     * 校验分区编码是否唯一
     *
     * @param region 游戏分区信息
     * @return true=唯一, false=不唯一
     */
    public boolean checkRegionCodeUnique(GameRegion region);

    /**
     * 生成并存储导出配置JSON
     *
     * @param regionId 分区ID
     * @return 配置JSON字符串
     */
    public String exportConfig(Long regionId);

    /**
     * 获取分区配置(从缓存或DB)
     *
     * @param regionId 分区ID
     * @return 配置JSON字符串
     */
    public String getRegionConfig(Long regionId);

    /**
     * 获取分区服务器列表(从缓存或DB)
     *
     * @param regionId 分区ID
     * @return 服务器JSON数组字符串
     */
    public String getRegionServers(Long regionId);

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
    public Long cloneRegion(Long regionId, String newName, String newCode, Long targetChannelId, boolean cloneServers);
}

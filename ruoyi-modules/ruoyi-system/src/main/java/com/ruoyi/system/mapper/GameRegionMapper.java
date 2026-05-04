package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.GameRegion;

/**
 * 游戏分区 数据层
 *
 * @author ruoyi
 */
public interface GameRegionMapper
{
    /**
     * 查询游戏分区列表
     *
     * @param region 游戏分区信息
     * @return 游戏分区集合
     */
    public List<GameRegion> selectGameRegionList(GameRegion region);

    /**
     * 根据分区ID查询游戏分区
     *
     * @param regionId 分区ID
     * @return 游戏分区
     */
    public GameRegion selectGameRegionById(Long regionId);

    /**
     * 根据渠道ID查询游戏分区列表
     *
     * @param channelId 渠道ID
     * @return 游戏分区集合
     */
    public List<GameRegion> selectGameRegionByChannelId(Long channelId);

    /**
     * 根据分区编码查询游戏分区
     *
     * @param regionCode 分区编码
     * @return 游戏分区
     */
    public GameRegion selectGameRegionByCode(String regionCode);

    /**
     * 根据代理分区key查询游戏分区
     *
     * @param proxyRegionKey 代理分区key
     * @return 游戏分区
     */
    public GameRegion selectGameRegionByKey(String proxyRegionKey);

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
     * 删除游戏分区
     *
     * @param regionId 分区ID
     * @return 结果
     */
    public int deleteGameRegionById(Long regionId);

    /**
     * 批量删除游戏分区
     *
     * @param regionIds 需要删除的分区ID
     * @return 结果
     */
    public int deleteGameRegionByIds(Long[] regionIds);

    /**
     * 统计指定分区ID下的子记录数
     *
     * @param regionId 分区ID
     * @return 记录数
     */
    public int countByRegionId(Long regionId);
}

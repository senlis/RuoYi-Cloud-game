package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.GameChannel;

/**
 * 游戏渠道 数据层
 *
 * @author ruoyi
 */
public interface GameChannelMapper
{
    /**
     * 查询游戏渠道列表
     *
     * @param channel 游戏渠道信息
     * @return 游戏渠道集合
     */
    public List<GameChannel> selectGameChannelList(GameChannel channel);

    /**
     * 根据渠道ID查询游戏渠道
     *
     * @param channelId 渠道ID
     * @return 游戏渠道
     */
    public GameChannel selectGameChannelById(Long channelId);

    /**
     * 根据项目ID查询游戏渠道列表
     *
     * @param projectId 项目ID
     * @return 游戏渠道集合
     */
    public List<GameChannel> selectGameChannelByProjectId(Long projectId);

    /**
     * 根据渠道编码查询游戏渠道
     *
     * @param channelCode 渠道编码
     * @return 游戏渠道
     */
    public GameChannel selectGameChannelByCode(String channelCode);

    /**
     * 新增游戏渠道
     *
     * @param channel 游戏渠道信息
     * @return 结果
     */
    public int insertGameChannel(GameChannel channel);

    /**
     * 修改游戏渠道
     *
     * @param channel 游戏渠道信息
     * @return 结果
     */
    public int updateGameChannel(GameChannel channel);

    /**
     * 删除游戏渠道
     *
     * @param channelId 渠道ID
     * @return 结果
     */
    public int deleteGameChannelById(Long channelId);

    /**
     * 批量删除游戏渠道
     *
     * @param channelIds 需要删除的渠道ID
     * @return 结果
     */
    public int deleteGameChannelByIds(Long[] channelIds);

    /**
     * 统计指定渠道ID下的子记录数
     *
     * @param channelId 渠道ID
     * @return 记录数
     */
    public int countByChannelId(Long channelId);
}

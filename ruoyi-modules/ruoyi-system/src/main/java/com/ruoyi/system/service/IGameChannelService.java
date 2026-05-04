package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.GameChannel;

/**
 * 游戏渠道 服务层
 *
 * @author ruoyi
 */
public interface IGameChannelService
{
    /**
     * 查询游戏渠道
     *
     * @param channelId 渠道ID
     * @return 游戏渠道
     */
    public GameChannel selectGameChannelById(Long channelId);

    /**
     * 查询游戏渠道列表
     *
     * @param channel 游戏渠道信息
     * @return 游戏渠道集合
     */
    public List<GameChannel> selectGameChannelList(GameChannel channel);

    /**
     * 根据项目ID查询游戏渠道列表
     *
     * @param projectId 项目ID
     * @return 游戏渠道集合
     */
    public List<GameChannel> selectGameChannelByProjectId(Long projectId);

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
     * 批量删除游戏渠道
     *
     * @param channelIds 需要删除的渠道ID
     * @return 结果
     */
    public int deleteGameChannelByIds(Long[] channelIds);

    /**
     * 校验渠道编码是否唯一
     *
     * @param channel 游戏渠道信息
     * @return true=唯一, false=不唯一
     */
    public boolean checkChannelCodeUnique(GameChannel channel);
}

package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.GameChannel;
import com.ruoyi.system.helper.GameAuthContext;
import com.ruoyi.system.mapper.GameChannelMapper;
import com.ruoyi.system.service.IGameChannelService;

/**
 * 游戏渠道 服务层实现
 *
 * @author ruoyi
 */
@Service
public class GameChannelServiceImpl implements IGameChannelService
{
    @Autowired
    private GameChannelMapper gameChannelMapper;

    @Autowired
    private GameAuthContext gameAuthContext;

    /**
     * 查询游戏渠道
     *
     * @param channelId 渠道ID
     * @return 游戏渠道
     */
    @Override
    public GameChannel selectGameChannelById(Long channelId)
    {
        return gameChannelMapper.selectGameChannelById(channelId);
    }

    /**
     * 查询游戏渠道列表
     *
     * @param channel 游戏渠道信息
     * @return 游戏渠道集合
     */
    @Override
    public List<GameChannel> selectGameChannelList(GameChannel channel)
    {
        // 非管理员用户，根据角色权限过滤渠道列表
        if (!gameAuthContext.isAdmin())
        {
            List<Long> authIds = gameAuthContext.getAuthChannelIds();
            if (authIds != null && !authIds.isEmpty())
            {
                channel.getParams().put("authChannelIds", authIds);
            }
            else if (authIds != null && authIds.isEmpty())
            {
                // 有角色但无权限，传入-1使查询结果为空
                channel.getParams().put("authChannelIds", java.util.Collections.singletonList(-1L));
            }
        }
        return gameChannelMapper.selectGameChannelList(channel);
    }

    /**
     * 根据项目ID查询游戏渠道列表
     *
     * @param projectId 项目ID
     * @return 游戏渠道集合
     */
    @Override
    public List<GameChannel> selectGameChannelByProjectId(Long projectId)
    {
        // 先查询该项目下所有可见渠道
        GameChannel query = new GameChannel();
        query.setProjectId(projectId);

        // 非管理员用户，根据角色权限过滤渠道列表
        if (!gameAuthContext.isAdmin())
        {
            List<Long> authIds = gameAuthContext.getAuthChannelIds();
            if (authIds != null && !authIds.isEmpty())
            {
                query.getParams().put("authChannelIds", authIds);
            }
            else if (authIds != null && authIds.isEmpty())
            {
                query.getParams().put("authChannelIds", java.util.Collections.singletonList(-1L));
            }
        }
        return gameChannelMapper.selectGameChannelList(query);
    }

    /**
     * 新增游戏渠道
     *
     * @param channel 游戏渠道信息
     * @return 结果
     */
    @Override
    public int insertGameChannel(GameChannel channel)
    {
        return gameChannelMapper.insertGameChannel(channel);
    }

    /**
     * 修改游戏渠道
     *
     * @param channel 游戏渠道信息
     * @return 结果
     */
    @Override
    public int updateGameChannel(GameChannel channel)
    {
        return gameChannelMapper.updateGameChannel(channel);
    }

    /**
     * 批量删除游戏渠道
     *
     * @param channelIds 需要删除的渠道ID
     * @return 结果
     */
    @Override
    public int deleteGameChannelByIds(Long[] channelIds)
    {
        return gameChannelMapper.deleteGameChannelByIds(channelIds);
    }

    /**
     * 校验渠道编码是否唯一
     *
     * @param channel 游戏渠道信息
     * @return true=唯一, false=不唯一
     */
    @Override
    public boolean checkChannelCodeUnique(GameChannel channel)
    {
        Long channelId = channel.getChannelId() == null ? -1L : channel.getChannelId();
        GameChannel info = gameChannelMapper.selectGameChannelByCode(channel.getChannelCode());
        if (info != null && !info.getChannelId().equals(channelId))
        {
            return false;
        }
        return true;
    }
}

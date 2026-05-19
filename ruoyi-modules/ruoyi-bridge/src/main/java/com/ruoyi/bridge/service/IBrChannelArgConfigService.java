package com.ruoyi.bridge.service;

import java.util.List;
import com.ruoyi.bridge.domain.BrChannelArgConfig;

/**
 * 渠道出包参数配置Service接口
 *
 * @author ruoyi
 */
public interface IBrChannelArgConfigService {

    /**
     * 查询渠道出包参数配置
     *
     * @param channelId 渠道ID
     * @return 渠道出包参数配置
     */
    public BrChannelArgConfig selectById(Long channelId);

    /**
     * 根据渠道标识和区域标识查询配置
     *
     * @param channelKey 渠道标识
     * @param regionKey  区域标识
     * @return 渠道出包参数配置
     */
    public BrChannelArgConfig selectByKey(String channelKey, String regionKey);

    /**
     * 查询渠道出包参数配置列表
     *
     * @param config 查询条件
     * @return 渠道出包参数配置集合
     */
    public List<BrChannelArgConfig> selectList(BrChannelArgConfig config);

    /**
     * 新增渠道出包参数配置
     *
     * @param config 渠道出包参数配置
     * @return 结果
     */
    public int insert(BrChannelArgConfig config);

    /**
     * 修改渠道出包参数配置
     *
     * @param config 渠道出包参数配置
     * @return 结果
     */
    public int update(BrChannelArgConfig config);

    /**
     * 批量删除渠道出包参数配置
     *
     * @param channelIds 需要删除的渠道ID
     * @return 结果
     */
    public int deleteByIds(Long[] channelIds);
}

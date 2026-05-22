package com.ruoyi.bridge.service;

import java.util.List;
import com.ruoyi.bridge.domain.BrPlatformConfig;

/**
 * 渠道接入平台配置Service接口
 *
 * @author ruoyi
 */
public interface IBrPlatformConfigService {

    /**
     * 根据平台ID查询配置
     *
     * @param platformId 平台ID
     * @return 渠道接入平台配置
     */
    public BrPlatformConfig selectById(Long platformId);

    /**
     * 根据渠道KEY查询配置
     *
     * @param channelKey 渠道KEY
     * @return 渠道接入平台配置
     */
    public BrPlatformConfig selectByChannelKey(String channelKey);

    /**
     * 查询渠道接入平台配置列表
     *
     * @param config 查询条件
     * @return 渠道接入平台配置集合
     */
    public List<BrPlatformConfig> selectList(BrPlatformConfig config);

    /**
     * 新增渠道接入平台配置
     *
     * @param config 渠道接入平台配置
     * @return 结果
     */
    public int insert(BrPlatformConfig config);

    /**
     * 修改渠道接入平台配置
     *
     * @param config 渠道接入平台配置
     * @return 结果
     */
    public int update(BrPlatformConfig config);

    /**
     * 删除渠道接入平台配置
     *
     * @param platformIds 需要删除的平台ID
     * @return 结果
     */
    public int deleteByIds(Long[] platformIds);

    /**
     * 查询所有已启用的平台配置
     *
     * @return 已启用的平台配置集合
     */
    public List<BrPlatformConfig> selectAllEnabled();
}

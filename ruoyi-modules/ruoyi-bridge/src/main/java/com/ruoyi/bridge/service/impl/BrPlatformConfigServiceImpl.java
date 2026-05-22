package com.ruoyi.bridge.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.bridge.domain.BrPlatformConfig;
import com.ruoyi.bridge.mapper.BrPlatformConfigMapper;
import com.ruoyi.bridge.service.IBrPlatformConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 渠道接入平台配置Service实现
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class BrPlatformConfigServiceImpl implements IBrPlatformConfigService {

    @Autowired
    private BrPlatformConfigMapper brPlatformConfigMapper;

    @Override
    public BrPlatformConfig selectById(Long platformId) {
        return brPlatformConfigMapper.selectById(platformId);
    }

    @Override
    public BrPlatformConfig selectByChannelKey(String channelKey) {
        return brPlatformConfigMapper.selectByChannelKey(channelKey);
    }

    @Override
    public List<BrPlatformConfig> selectList(BrPlatformConfig config) {
        return brPlatformConfigMapper.selectList(config);
    }

    @Override
    public int insert(BrPlatformConfig config) {
        return brPlatformConfigMapper.insert(config);
    }

    @Override
    public int update(BrPlatformConfig config) {
        return brPlatformConfigMapper.update(config);
    }

    @Override
    public int deleteByIds(Long[] platformIds) {
        return brPlatformConfigMapper.deleteByIds(platformIds);
    }

    @Override
    public List<BrPlatformConfig> selectAllEnabled() {
        return brPlatformConfigMapper.selectAllEnabled();
    }
}

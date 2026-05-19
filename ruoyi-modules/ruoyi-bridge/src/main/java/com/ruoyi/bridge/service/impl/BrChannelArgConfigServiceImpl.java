package com.ruoyi.bridge.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.bridge.domain.BrChannelArgConfig;
import com.ruoyi.bridge.mapper.BrChannelArgConfigMapper;
import com.ruoyi.bridge.service.IBrChannelArgConfigService;

/**
 * 渠道出包参数配置Service实现
 *
 * @author ruoyi
 */
@Service
public class BrChannelArgConfigServiceImpl implements IBrChannelArgConfigService {

    private static final Logger log = LoggerFactory.getLogger(BrChannelArgConfigServiceImpl.class);

    @Autowired
    private BrChannelArgConfigMapper brChannelArgConfigMapper;

    @Override
    public BrChannelArgConfig selectById(Long channelId) {
        return brChannelArgConfigMapper.selectById(channelId);
    }

    @Override
    public BrChannelArgConfig selectByKey(String channelKey, String regionKey) {
        return brChannelArgConfigMapper.selectByKey(channelKey, regionKey);
    }

    @Override
    public List<BrChannelArgConfig> selectList(BrChannelArgConfig config) {
        return brChannelArgConfigMapper.selectList(config);
    }

    @Override
    public int insert(BrChannelArgConfig config) {
        return brChannelArgConfigMapper.insert(config);
    }

    @Override
    public int update(BrChannelArgConfig config) {
        return brChannelArgConfigMapper.update(config);
    }

    @Override
    public int deleteByIds(Long[] channelIds) {
        return brChannelArgConfigMapper.deleteByIds(channelIds);
    }
}

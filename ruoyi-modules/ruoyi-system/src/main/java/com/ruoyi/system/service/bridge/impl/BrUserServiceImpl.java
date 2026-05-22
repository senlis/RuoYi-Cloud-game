package com.ruoyi.system.service.bridge.impl;

import com.ruoyi.system.datasource.ChannelDs;
import com.ruoyi.system.domain.BrUser;
import com.ruoyi.system.mapper.BrUserMapper;
import com.ruoyi.system.service.bridge.IBrUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息Service实现
 * <p>
 * 通过 @ChannelDs 自动切换数据源到对应渠道的独立数据库。
 *
 * @author ruoyi
 */
@Service
public class BrUserServiceImpl implements IBrUserService {

    @Autowired
    private BrUserMapper brUserMapper;

    @ChannelDs("#channelKey")
    @Override
    public List<BrUser> selectUserList(String channelKey, BrUser user) {
        user.setChannelKey(channelKey);
        return brUserMapper.selectList(user);
    }
}

package com.ruoyi.system.service.bridge.impl;

import com.ruoyi.system.datasource.ChannelDs;
import com.ruoyi.system.domain.BrRole;
import com.ruoyi.system.mapper.BrRoleMapper;
import com.ruoyi.system.service.bridge.IBrRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色信息Service实现
 * <p>
 * 通过 @ChannelDs 自动切换数据源到对应渠道的独立数据库。
 *
 * @author ruoyi
 */
@Service
public class BrRoleServiceImpl implements IBrRoleService {

    @Autowired
    private BrRoleMapper brRoleMapper;

    @ChannelDs("#channelKey")
    @Override
    public List<BrRole> selectRoleList(String channelKey, BrRole role) {
        role.setChannelKey(channelKey);
        return brRoleMapper.selectList(role);
    }
}

package com.ruoyi.bridge.service.impl;

import com.ruoyi.bridge.datasource.ChannelDsHelper;
import com.ruoyi.bridge.domain.BrUser;
import com.ruoyi.bridge.domain.dto.AuthRequest;
import com.ruoyi.bridge.domain.dto.AuthResponse;
import com.ruoyi.bridge.mapper.BrUserMapper;
import com.ruoyi.bridge.service.IBrUserService;
import com.ruoyi.bridge.service.ProjectKeyHelper;
import com.ruoyi.common.core.utils.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 渠道用户Service实现
 *
 * @author ruoyi
 */
@Service
public class BrUserServiceImpl implements IBrUserService {

    private static final Logger log = LoggerFactory.getLogger(BrUserServiceImpl.class);

    @Autowired
    private BrUserMapper brUserMapper;

    @Autowired
    private ChannelDsHelper channelDsHelper;

    @Autowired
    private ProjectKeyHelper projectKeyHelper;

    @Override
    public BrUser selectByUserId(Long userId) {
        return brUserMapper.selectByUserId(userId);
    }

    @Override
    public BrUser selectByIdentityId(String identityId) {
        return brUserMapper.selectByIdentityId(identityId);
    }

    @Override
    public AuthResponse loginOrCreate(AuthRequest request) {
        // 1. 在主库查询项目 md5key（用于响应签名，与游戏服通信）
        String md5Key = projectKeyHelper.getMd5KeyByChannel(request.getChannelKey());

        // 2. 通过 ChannelDsHelper 切到渠道库执行用户操作
        return channelDsHelper.execute(request.getChannelKey(), () -> {
            BrUser existing = brUserMapper.selectByUserKey(
                    request.getChannelKey(), request.getUserId(), request.getServerId());

            if (existing != null) {
                existing.setLastLoginTime(new Date());
                brUserMapper.update(existing);
                return buildAuthResponse(existing.getIdentityId(), existing.getUserId(),
                        existing.getServerId(), existing.getChannelKey(), md5Key);
            }

            BrUser newUser = new BrUser();
            String identityId = UUID.randomUUID().toString().replace("-", "");
            newUser.setIdentityId(identityId);
            newUser.setChannelKey(request.getChannelKey());
            newUser.setChannelUserId(request.getUserId());
            newUser.setServerId(request.getServerId());
            newUser.setLastLoginTime(new Date());
            brUserMapper.insert(newUser);

            return buildAuthResponse(newUser.getIdentityId(), newUser.getUserId(),
                    newUser.getServerId(), newUser.getChannelKey(), md5Key);
        });
    }

    @Override
    public List<BrUser> selectList(BrUser user) {
        return brUserMapper.selectList(user);
    }

    @Override
    public int insert(BrUser user) {
        return brUserMapper.insert(user);
    }

    @Override
    public int update(BrUser user) {
        return brUserMapper.update(user);
    }

    @Override
    public int deleteByIds(Long[] userIds) {
        return brUserMapper.deleteByIds(userIds);
    }

    private AuthResponse buildAuthResponse(String identityId, Long userId, Integer serverId,
                                            String channelKey, String md5Key) {
        AuthResponse response = new AuthResponse();
        response.setIdentityId(identityId);
        response.setUserId(String.valueOf(userId));
        response.setServerId(serverId);
        response.setChannelKey(channelKey);
        response.setTimestamp(System.currentTimeMillis());

        java.util.Map<String, String> params = new java.util.LinkedHashMap<>();
        params.put("identityId", identityId);
        params.put("userId", String.valueOf(userId));
        params.put("serverId", String.valueOf(serverId));
        params.put("channelKey", channelKey);
        params.put("timestamp", String.valueOf(response.getTimestamp()));

        String sign = SignUtils.md5Sign(params, md5Key);
        response.setSign(sign);
        return response;
    }

}

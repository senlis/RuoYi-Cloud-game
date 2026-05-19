package com.ruoyi.bridge.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.bridge.domain.BrUser;
import com.ruoyi.bridge.domain.dto.AuthRequest;
import com.ruoyi.bridge.domain.dto.AuthResponse;
import com.ruoyi.bridge.mapper.BrUserMapper;
import com.ruoyi.bridge.service.IBrUserService;

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
        // 查找已有用户
        BrUser existing = brUserMapper.selectByUserKey(
                request.getChannelKey(),
                request.getUserId(),
                request.getServerId());

        if (existing != null) {
            // 更新最后登录时间
            existing.setLastLoginTime(new Date());
            brUserMapper.update(existing);

            AuthResponse response = new AuthResponse();
            response.setIdentityId(existing.getIdentityId());
            response.setUserId(String.valueOf(existing.getUserId()));
            response.setServerId(existing.getServerId());
            response.setChannelKey(existing.getChannelKey());
            response.setTimestamp(System.currentTimeMillis());
            return response;
        }

        // 创建新用户
        BrUser newUser = new BrUser();
        String identityId = UUID.randomUUID().toString().replace("-", "");
        newUser.setIdentityId(identityId);
        newUser.setChannelKey(request.getChannelKey());
        newUser.setChannelUserId(request.getUserId());
        newUser.setServerId(request.getServerId());
        newUser.setLastLoginTime(new Date());
        brUserMapper.insert(newUser);

        AuthResponse response = new AuthResponse();
        response.setIdentityId(newUser.getIdentityId());
        response.setUserId(String.valueOf(newUser.getUserId()));
        response.setServerId(newUser.getServerId());
        response.setChannelKey(newUser.getChannelKey());
        response.setTimestamp(System.currentTimeMillis());
        return response;
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
}

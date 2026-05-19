package com.ruoyi.bridge.service;

import java.util.List;
import com.ruoyi.bridge.domain.BrUser;
import com.ruoyi.bridge.domain.dto.AuthRequest;
import com.ruoyi.bridge.domain.dto.AuthResponse;

/**
 * 渠道用户Service接口
 *
 * @author ruoyi
 */
public interface IBrUserService {

    /**
     * 查询用户
     *
     * @param userId 用户ID
     * @return 渠道用户
     */
    public BrUser selectByUserId(Long userId);

    /**
     * 根据平台标识查询用户
     *
     * @param identityId 平台唯一标识
     * @return 渠道用户
     */
    public BrUser selectByIdentityId(String identityId);

    /**
     * 登录或创建用户
     * 根据渠道标识+渠道用户ID+服务器ID查找已有用户，不存在则创建新用户
     *
     * @param request 认证请求
     * @return 认证响应
     */
    public AuthResponse loginOrCreate(AuthRequest request);

    /**
     * 查询用户列表
     *
     * @param user 查询条件
     * @return 用户集合
     */
    public List<BrUser> selectList(BrUser user);

    /**
     * 新增用户
     *
     * @param user 渠道用户
     * @return 结果
     */
    public int insert(BrUser user);

    /**
     * 修改用户
     *
     * @param user 渠道用户
     * @return 结果
     */
    public int update(BrUser user);

    /**
     * 批量删除用户
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    public int deleteByIds(Long[] userIds);
}

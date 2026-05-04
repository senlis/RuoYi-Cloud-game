package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.GameRoleAuth;
import com.ruoyi.system.mapper.GameRoleAuthMapper;
import com.ruoyi.system.service.IGameRoleAuthService;

/**
 * 游戏角色权限 服务层实现
 *
 * @author ruoyi
 */
@Service
public class GameRoleAuthServiceImpl implements IGameRoleAuthService
{
    @Autowired
    private GameRoleAuthMapper gameRoleAuthMapper;

    /**
     * 查询游戏角色权限列表
     *
     * @param gameRoleAuth 查询条件
     * @return 游戏角色权限集合
     */
    @Override
    public List<GameRoleAuth> selectGameRoleAuthList(GameRoleAuth gameRoleAuth)
    {
        return gameRoleAuthMapper.selectGameRoleAuthList(gameRoleAuth);
    }

    /**
     * 根据权限ID查询游戏角色权限
     *
     * @param authId 权限ID
     * @return 游戏角色权限
     */
    @Override
    public GameRoleAuth selectGameRoleAuthById(Long authId)
    {
        return gameRoleAuthMapper.selectGameRoleAuthById(authId);
    }

    /**
     * 查询用户授权的实体ID列表
     *
     * @param userId 用户ID
     * @param entityType 实体类型(project, channel, region)
     * @return 授权的实体ID列表
     */
    @Override
    public List<Long> selectAuthEntityIds(Long userId, String entityType)
    {
        // 如果用户是超级管理员(roleId=1)，查看所有数据
        if (isAdmin(userId))
        {
            return null;
        }
        return gameRoleAuthMapper.selectByUserIdAndType(userId, entityType);
    }

    /**
     * 查询角色在指定实体类型下的授权实体ID列表
     *
     * @param roleId 角色ID
     * @param entityType 实体类型
     * @return 实体ID列表
     */
    @Override
    public List<Long> selectEntityIdsByRoleAndType(Long roleId, String entityType)
    {
        return gameRoleAuthMapper.selectByRoleAndType(roleId, entityType);
    }

    /**
     * 判断用户是否为超级管理员(角色ID=1)
     * 通过查询用户角色关联表(sys_user_role)，判断是否拥有role_id=1的角色
     *
     * @param userId 用户ID
     * @return true=管理员
     */
    @Override
    public boolean isAdmin(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        return gameRoleAuthMapper.countAdminRoleByUserId(userId) > 0;
    }

    /**
     * 保存角色权限(先删除原有权限，再批量新增)
     *
     * @param roleId 角色ID
     * @param entityType 实体类型
     * @param entityIds 实体ID列表
     * @return 结果
     */
    @Override
    @Transactional
    public int saveRoleAuth(Long roleId, String entityType, List<Long> entityIds)
    {
        // 先删除该角色在该实体类型下的所有权限
        gameRoleAuthMapper.deleteByRoleAndType(roleId, entityType);

        // 如果没有新增的权限，直接返回
        if (entityIds == null || entityIds.isEmpty())
        {
            return 0;
        }

        // 批量新增
        int count = 0;
        for (Long entityId : entityIds)
        {
            GameRoleAuth auth = new GameRoleAuth();
            auth.setRoleId(roleId);
            auth.setEntityType(entityType);
            auth.setEntityId(entityId);
            count += gameRoleAuthMapper.insertGameRoleAuth(auth);
        }
        return count;
    }

    /**
     * 批量删除游戏角色权限
     *
     * @param authIds 需要删除的权限ID
     * @return 结果
     */
    @Override
    public int deleteGameRoleAuthByIds(Long[] authIds)
    {
        return gameRoleAuthMapper.deleteGameRoleAuthByIds(authIds);
    }

    /**
     * 根据角色ID删除所有权限
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int deleteByRoleId(Long roleId)
    {
        return gameRoleAuthMapper.deleteByRoleId(roleId);
    }
}

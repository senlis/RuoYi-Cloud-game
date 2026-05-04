package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.GameRoleAuth;

/**
 * 游戏角色权限 服务层
 *
 * @author ruoyi
 */
public interface IGameRoleAuthService
{
    /**
     * 查询游戏角色权限列表
     *
     * @param gameRoleAuth 查询条件
     * @return 游戏角色权限集合
     */
    public List<GameRoleAuth> selectGameRoleAuthList(GameRoleAuth gameRoleAuth);

    /**
     * 根据权限ID查询游戏角色权限
     *
     * @param authId 权限ID
     * @return 游戏角色权限
     */
    public GameRoleAuth selectGameRoleAuthById(Long authId);

    /**
     * 查询用户授权的实体ID列表
     *
     * @param userId 用户ID
     * @param entityType 实体类型(project, channel, region)
     * @return 授权的实体ID列表
     */
    public List<Long> selectAuthEntityIds(Long userId, String entityType);

    /**
     * 查询角色在指定实体类型下的授权实体ID列表
     *
     * @param roleId 角色ID
     * @param entityType 实体类型
     * @return 实体ID列表
     */
    public List<Long> selectEntityIdsByRoleAndType(Long roleId, String entityType);

    /**
     * 判断用户是否为超级管理员(角色ID=1)
     *
     * @param userId 用户ID
     * @return true=管理员
     */
    public boolean isAdmin(Long userId);

    /**
     * 保存角色权限(先删除原有权限，再批量新增)
     *
     * @param roleId 角色ID
     * @param entityType 实体类型
     * @param entityIds 实体ID列表
     * @return 结果
     */
    public int saveRoleAuth(Long roleId, String entityType, List<Long> entityIds);

    /**
     * 批量删除游戏角色权限
     *
     * @param authIds 需要删除的权限ID
     * @return 结果
     */
    public int deleteGameRoleAuthByIds(Long[] authIds);

    /**
     * 根据角色ID删除所有权限
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteByRoleId(Long roleId);
}

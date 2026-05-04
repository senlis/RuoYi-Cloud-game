package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.GameRoleAuth;

/**
 * 游戏角色权限Mapper接口
 *
 * @author ruoyi
 */
public interface GameRoleAuthMapper
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
     * 根据角色ID和实体类型查询实体ID列表
     *
     * @param roleId 角色ID
     * @param entityType 实体类型
     * @return 实体ID列表
     */
    public List<Long> selectByRoleAndType(Long roleId, String entityType);

    /**
     * 根据用户ID和实体类型查询授权的实体ID列表
     *
     * @param userId 用户ID
     * @param entityType 实体类型
     * @return 实体ID列表
     */
    public List<Long> selectByUserIdAndType(Long userId, String entityType);

    /**
     * 新增游戏角色权限
     *
     * @param gameRoleAuth 游戏角色权限
     * @return 结果
     */
    public int insertGameRoleAuth(GameRoleAuth gameRoleAuth);

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

    /**
     * 根据角色ID和实体类型删除权限
     *
     * @param roleId 角色ID
     * @param entityType 实体类型
     * @return 结果
     */
    public int deleteByRoleAndType(Long roleId, String entityType);

    /**
     * 查询用户是否拥有超级管理员角色(role_id=1)
     *
     * @param userId 用户ID
     * @return 角色数量(大于0表示是管理员)
     */
    public int countAdminRoleByUserId(Long userId);
}

package com.ruoyi.system.helper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.service.IGameRoleAuthService;

/**
 * 游戏权限上下文，提供当前用户的权限判断
 *
 * @author ruoyi
 */
@Component
public class GameAuthContext
{
    @Autowired
    private IGameRoleAuthService gameRoleAuthService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId()
    {
        try
        {
            return SecurityUtils.getLoginUser().getUserid();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 判断当前用户是否为超级管理员(角色ID=1)
     *
     * @return true=管理员，无需进行权限过滤
     */
    public boolean isAdmin()
    {
        Long userId = getCurrentUserId();
        if (userId == null)
        {
            return false;
        }
        return gameRoleAuthService.isAdmin(userId);
    }

    /**
     * 判断当前用户是否为超级管理员(角色ID=1)
     *
     * @param userId 用户ID
     * @return true=管理员
     */
    public boolean isAdmin(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        return gameRoleAuthService.isAdmin(userId);
    }

    /**
     * 获取当前用户授权的项目ID列表
     * 返回null表示管理员，可查看所有数据
     *
     * @return 项目ID列表，或null(全部)
     */
    public List<Long> getAuthProjectIds()
    {
        Long userId = getCurrentUserId();
        if (userId == null)
        {
            return null;
        }
        return gameRoleAuthService.selectAuthEntityIds(userId, "project");
    }

    /**
     * 获取当前用户授权的渠道ID列表
     * 返回null表示管理员，可查看所有数据
     *
     * @return 渠道ID列表，或null(全部)
     */
    public List<Long> getAuthChannelIds()
    {
        Long userId = getCurrentUserId();
        if (userId == null)
        {
            return null;
        }
        return gameRoleAuthService.selectAuthEntityIds(userId, "channel");
    }

    /**
     * 获取当前用户授权的分区ID列表
     * 返回null表示管理员，可查看所有数据
     *
     * @return 分区ID列表，或null(全部)
     */
    public List<Long> getAuthRegionIds()
    {
        Long userId = getCurrentUserId();
        if (userId == null)
        {
            return null;
        }
        return gameRoleAuthService.selectAuthEntityIds(userId, "region");
    }

    /**
     * 获取用户授权的项目ID列表
     *
     * @param userId 用户ID
     * @return 项目ID列表，或null(全部)
     */
    public List<Long> getAuthProjectIds(Long userId)
    {
        return gameRoleAuthService.selectAuthEntityIds(userId, "project");
    }

    /**
     * 获取用户授权的渠道ID列表
     *
     * @param userId 用户ID
     * @return 渠道ID列表，或null(全部)
     */
    public List<Long> getAuthChannelIds(Long userId)
    {
        return gameRoleAuthService.selectAuthEntityIds(userId, "channel");
    }

    /**
     * 获取用户授权的分区ID列表
     *
     * @param userId 用户ID
     * @return 分区ID列表，或null(全部)
     */
    public List<Long> getAuthRegionIds(Long userId)
    {
        return gameRoleAuthService.selectAuthEntityIds(userId, "region");
    }
}

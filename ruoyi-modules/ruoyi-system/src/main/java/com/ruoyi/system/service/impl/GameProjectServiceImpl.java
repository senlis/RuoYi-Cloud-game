package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.GameProject;
import com.ruoyi.system.helper.GameAuthContext;
import com.ruoyi.system.mapper.GameProjectMapper;
import com.ruoyi.system.service.IGameProjectService;

/**
 * 游戏项目 服务层实现
 *
 * @author ruoyi
 */
@Service
public class GameProjectServiceImpl implements IGameProjectService
{
    @Autowired
    private GameProjectMapper gameProjectMapper;

    @Autowired
    private GameAuthContext gameAuthContext;

    /**
     * 查询游戏项目
     *
     * @param projectId 项目ID
     * @return 游戏项目
     */
    @Override
    public GameProject selectGameProjectById(Long projectId)
    {
        return gameProjectMapper.selectGameProjectById(projectId);
    }

    /**
     * 查询游戏项目列表
     *
     * @param project 游戏项目信息
     * @return 游戏项目集合
     */
    @Override
    public List<GameProject> selectGameProjectList(GameProject project)
    {
        // 非管理员用户，根据角色权限过滤项目列表
        if (!gameAuthContext.isAdmin())
        {
            List<Long> authIds = gameAuthContext.getAuthProjectIds();
            if (authIds != null && !authIds.isEmpty())
            {
                project.getParams().put("authProjectIds", authIds);
            }
            else if (authIds != null && authIds.isEmpty())
            {
                // 有角色但无权限，传入-1使查询结果为空
                project.getParams().put("authProjectIds", java.util.Collections.singletonList(-1L));
            }
            // authIds == null means admin, no filtering needed
        }
        return gameProjectMapper.selectGameProjectList(project);
    }

    /**
     * 新增游戏项目
     *
     * @param project 游戏项目信息
     * @return 结果
     */
    @Override
    public int insertGameProject(GameProject project)
    {
        return gameProjectMapper.insertGameProject(project);
    }

    /**
     * 修改游戏项目
     *
     * @param project 游戏项目信息
     * @return 结果
     */
    @Override
    public int updateGameProject(GameProject project)
    {
        return gameProjectMapper.updateGameProject(project);
    }

    /**
     * 批量删除游戏项目
     *
     * @param projectIds 需要删除的项目ID
     * @return 结果
     */
    @Override
    public int deleteGameProjectByIds(Long[] projectIds)
    {
        return gameProjectMapper.deleteGameProjectByIds(projectIds);
    }

    /**
     * 校验项目编码是否唯一
     *
     * @param project 游戏项目信息
     * @return true=唯一, false=不唯一
     */
    @Override
    public boolean checkProjectCodeUnique(GameProject project)
    {
        Long projectId = project.getProjectId() == null ? -1L : project.getProjectId();
        GameProject info = gameProjectMapper.selectGameProjectByCode(project.getProjectCode());
        if (info != null && !info.getProjectId().equals(projectId))
        {
            return false;
        }
        return true;
    }
}

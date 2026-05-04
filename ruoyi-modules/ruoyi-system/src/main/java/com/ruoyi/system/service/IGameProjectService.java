package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.GameProject;

/**
 * 游戏项目 服务层
 *
 * @author ruoyi
 */
public interface IGameProjectService
{
    /**
     * 查询游戏项目
     *
     * @param projectId 项目ID
     * @return 游戏项目
     */
    public GameProject selectGameProjectById(Long projectId);

    /**
     * 查询游戏项目列表
     *
     * @param project 游戏项目信息
     * @return 游戏项目集合
     */
    public List<GameProject> selectGameProjectList(GameProject project);

    /**
     * 新增游戏项目
     *
     * @param project 游戏项目信息
     * @return 结果
     */
    public int insertGameProject(GameProject project);

    /**
     * 修改游戏项目
     *
     * @param project 游戏项目信息
     * @return 结果
     */
    public int updateGameProject(GameProject project);

    /**
     * 批量删除游戏项目
     *
     * @param projectIds 需要删除的项目ID
     * @return 结果
     */
    public int deleteGameProjectByIds(Long[] projectIds);

    /**
     * 校验项目编码是否唯一
     *
     * @param project 游戏项目信息
     * @return true=唯一, false=不唯一
     */
    public boolean checkProjectCodeUnique(GameProject project);
}

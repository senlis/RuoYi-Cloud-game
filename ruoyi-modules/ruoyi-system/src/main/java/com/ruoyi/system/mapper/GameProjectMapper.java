package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.GameProject;

/**
 * 游戏项目 数据层
 *
 * @author ruoyi
 */
public interface GameProjectMapper
{
    /**
     * 查询游戏项目列表
     *
     * @param project 游戏项目信息
     * @return 游戏项目集合
     */
    public List<GameProject> selectGameProjectList(GameProject project);

    /**
     * 根据项目ID查询游戏项目
     *
     * @param projectId 项目ID
     * @return 游戏项目
     */
    public GameProject selectGameProjectById(Long projectId);

    /**
     * 根据项目编码查询游戏项目
     *
     * @param projectCode 项目编码
     * @return 游戏项目
     */
    public GameProject selectGameProjectByCode(String projectCode);

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
     * 删除游戏项目
     *
     * @param projectId 项目ID
     * @return 结果
     */
    public int deleteGameProjectById(Long projectId);

    /**
     * 批量删除游戏项目
     *
     * @param projectIds 需要删除的项目ID
     * @return 结果
     */
    public int deleteGameProjectByIds(Long[] projectIds);
}

package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.GameFieldDefine;

/**
 * 动态字段定义 服务层
 *
 * @author ruoyi
 */
public interface IGameFieldDefineService
{
    /**
     * 查询动态字段定义
     *
     * @param fieldId 字段ID
     * @return 动态字段定义
     */
    public GameFieldDefine selectGameFieldDefineById(Long fieldId);

    /**
     * 查询动态字段定义列表
     *
     * @param fieldDefine 动态字段定义信息
     * @return 动态字段定义集合
     */
    public List<GameFieldDefine> selectGameFieldDefineList(GameFieldDefine fieldDefine);

    /**
     * 根据实体类型查询启用的动态字段定义
     *
     * @param entityType 实体类型
     * @return 动态字段定义集合
     */
    public List<GameFieldDefine> selectActiveFieldByEntity(String entityType);

    /**
     * 新增动态字段定义
     *
     * @param fieldDefine 动态字段定义信息
     * @return 结果
     */
    public int insertGameFieldDefine(GameFieldDefine fieldDefine);

    /**
     * 修改动态字段定义
     *
     * @param fieldDefine 动态字段定义信息
     * @return 结果
     */
    public int updateGameFieldDefine(GameFieldDefine fieldDefine);

    /**
     * 批量删除动态字段定义
     *
     * @param fieldIds 需要删除的字段ID
     * @return 结果
     */
    public int deleteGameFieldDefineByIds(Long[] fieldIds);
}

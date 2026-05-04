package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.GameFieldDefine;
import com.ruoyi.system.mapper.GameFieldDefineMapper;
import com.ruoyi.system.service.IGameFieldDefineService;

/**
 * 动态字段定义 服务层实现
 *
 * @author ruoyi
 */
@Service
public class GameFieldDefineServiceImpl implements IGameFieldDefineService
{
    @Autowired
    private GameFieldDefineMapper gameFieldDefineMapper;

    /**
     * 查询动态字段定义
     *
     * @param fieldId 字段ID
     * @return 动态字段定义
     */
    @Override
    public GameFieldDefine selectGameFieldDefineById(Long fieldId)
    {
        return gameFieldDefineMapper.selectGameFieldDefineById(fieldId);
    }

    /**
     * 查询动态字段定义列表
     *
     * @param fieldDefine 动态字段定义信息
     * @return 动态字段定义集合
     */
    @Override
    public List<GameFieldDefine> selectGameFieldDefineList(GameFieldDefine fieldDefine)
    {
        return gameFieldDefineMapper.selectGameFieldDefineList(fieldDefine);
    }

    /**
     * 根据实体类型查询启用的动态字段定义
     *
     * @param entityType 实体类型
     * @return 动态字段定义集合
     */
    @Override
    public List<GameFieldDefine> selectActiveFieldByEntity(String entityType)
    {
        GameFieldDefine define = new GameFieldDefine();
        define.setEntityType(entityType);
        define.setStatus("0");
        return gameFieldDefineMapper.selectGameFieldDefineList(define);
    }

    /**
     * 新增动态字段定义
     *
     * @param fieldDefine 动态字段定义信息
     * @return 结果
     */
    @Override
    public int insertGameFieldDefine(GameFieldDefine fieldDefine)
    {
        return gameFieldDefineMapper.insertGameFieldDefine(fieldDefine);
    }

    /**
     * 修改动态字段定义
     *
     * @param fieldDefine 动态字段定义信息
     * @return 结果
     */
    @Override
    public int updateGameFieldDefine(GameFieldDefine fieldDefine)
    {
        return gameFieldDefineMapper.updateGameFieldDefine(fieldDefine);
    }

    /**
     * 批量删除动态字段定义
     *
     * @param fieldIds 需要删除的字段ID
     * @return 结果
     */
    @Override
    public int deleteGameFieldDefineByIds(Long[] fieldIds)
    {
        return gameFieldDefineMapper.deleteGameFieldDefineByIds(fieldIds);
    }
}

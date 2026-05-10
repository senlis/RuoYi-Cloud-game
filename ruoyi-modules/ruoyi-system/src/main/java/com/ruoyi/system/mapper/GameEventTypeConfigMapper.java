package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.GameEventTypeConfig;

import java.util.List;

/**
 * 事件类型配置 Mapper 接口
 *
 * @author ruoyi
 */
public interface GameEventTypeConfigMapper {

    /**
     * 查询事件类型列表
     */
    List<GameEventTypeConfig> selectList(GameEventTypeConfig config);

    /**
     * 根据 ID 查询
     */
    GameEventTypeConfig selectById(Long id);

    /**
     * 根据 eventType 查询
     */
    GameEventTypeConfig selectByEventType(String eventType);

    /**
     * 新增
     */
    int insert(GameEventTypeConfig config);

    /**
     * 修改
     */
    int update(GameEventTypeConfig config);

    /**
     * 删除
     */
    int deleteById(Long id);

    /**
     * 批量删除
     */
    int deleteByIds(Long[] ids);
}

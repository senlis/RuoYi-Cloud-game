package com.ruoyi.system.service;

import com.ruoyi.system.domain.GameEventTypeConfig;

import java.util.List;

/**
 * 事件类型配置 Service 接口
 *
 * @author ruoyi
 */
public interface IGameEventTypeConfigService {

    List<GameEventTypeConfig> selectList(GameEventTypeConfig config);

    GameEventTypeConfig selectById(Long id);

    GameEventTypeConfig selectByEventType(String eventType);

    int insert(GameEventTypeConfig config);

    int update(GameEventTypeConfig config);

    int deleteByIds(Long[] ids);
}

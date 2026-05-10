package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.GameEventTypeConfig;
import com.ruoyi.system.mapper.GameEventTypeConfigMapper;
import com.ruoyi.system.service.IGameEventTypeConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 事件类型配置 Service 实现
 *
 * @author ruoyi
 */
@Service
public class GameEventTypeConfigServiceImpl implements IGameEventTypeConfigService {

    private final GameEventTypeConfigMapper mapper;

    public GameEventTypeConfigServiceImpl(GameEventTypeConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<GameEventTypeConfig> selectList(GameEventTypeConfig config) {
        return mapper.selectList(config);
    }

    @Override
    public GameEventTypeConfig selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public GameEventTypeConfig selectByEventType(String eventType) {
        return mapper.selectByEventType(eventType);
    }

    @Override
    public int insert(GameEventTypeConfig config) {
        return mapper.insert(config);
    }

    @Override
    public int update(GameEventTypeConfig config) {
        return mapper.update(config);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        return mapper.deleteByIds(ids);
    }
}

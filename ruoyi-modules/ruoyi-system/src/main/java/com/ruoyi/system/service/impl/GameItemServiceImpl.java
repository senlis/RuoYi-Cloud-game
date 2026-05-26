package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.GameItem;
import com.ruoyi.system.mapper.GameItemMapper;
import com.ruoyi.system.service.IGameItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 游戏道具配置Service实现
 *
 * @author ruoyi
 */
@Service
public class GameItemServiceImpl implements IGameItemService {

    @Autowired
    private GameItemMapper gameItemMapper;

    @Override
    public List<GameItem> selectGameItemList(GameItem item) {
        return gameItemMapper.selectList(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importGameItem(Long projectId, List<GameItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        // 清空该项目下旧数据
        gameItemMapper.deleteByProject(projectId);
        // 设置项目ID
        for (GameItem item : items) {
            item.setProjectId(projectId);
        }
        return gameItemMapper.batchInsert(items);
    }

    @Override
    public int deleteGameItemById(Long projectId, Long itemId) {
        return gameItemMapper.deleteByProjectAndId(projectId, itemId);
    }

    @Override
    public void clearByProject(Long projectId) {
        gameItemMapper.deleteByProject(projectId);
    }
}

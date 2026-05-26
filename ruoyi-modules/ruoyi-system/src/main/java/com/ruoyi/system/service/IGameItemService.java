package com.ruoyi.system.service;

import com.ruoyi.system.domain.GameItem;

import java.util.List;

/**
 * 游戏道具配置Service接口
 *
 * @author ruoyi
 */
public interface IGameItemService {

    /**
     * 查询道具列表
     */
    List<GameItem> selectGameItemList(GameItem item);

    /**
     * 导入道具（全量覆盖：先清空该项目下旧数据，再插入）
     *
     * @param projectId 项目ID
     * @param items     道具列表
     * @return 导入数量
     */
    int importGameItem(Long projectId, List<GameItem> items);

    /**
     * 删除道具
     */
    int deleteGameItemById(Long projectId, Long itemId);

    /**
     * 清空指定项目下所有道具
     */
    void clearByProject(Long projectId);
}

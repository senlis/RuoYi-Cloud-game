package com.ruoyi.system.service;

/**
 * 游戏数据源管理服务
 * 负责动态注册、切换和刷新游戏库/日志库数据源
 *
 * @author ruoyi
 */
public interface IGameDataSourceService
{
    /** 游戏数据源前缀 */
    String GAME_DS_PREFIX = "game_db_";
    /** 日志数据源前缀 */
    String LOG_DS_PREFIX = "log_db_";

    /**
     * 获取或创建游戏数据库数据源，并切换到该数据源
     *
     * @param regionId 分区ID
     * @param serverId 服务器ID
     * @return 数据源名称
     */
    String switchToGameDb(Long regionId, Integer serverId);

    /**
     * 获取或创建日志数据库数据源，并切换到该数据源
     *
     * @param regionId 分区ID
     * @param serverId 服务器ID
     * @return 数据源名称
     */
    String switchToLogDb(Long regionId, Integer serverId);

    /**
     * 切换到主库（默认数据源）
     */
    void switchToMaster();

    /**
     * 刷新指定服务器的游戏数据源（清除缓存，下次使用时重新创建）
     *
     * @param regionId 分区ID
     * @param serverId 服务器ID
     */
    void refreshGameDb(Long regionId, Integer serverId);

    /**
     * 刷新指定服务器的日志数据源
     *
     * @param regionId 分区ID
     * @param serverId 服务器ID
     */
    void refreshLogDb(Long regionId, Integer serverId);

    /**
     * 刷新指定服务器的所有数据源
     *
     * @param regionId 分区ID
     * @param serverId 服务器ID
     */
    void refreshAll(Long regionId, Integer serverId);
}

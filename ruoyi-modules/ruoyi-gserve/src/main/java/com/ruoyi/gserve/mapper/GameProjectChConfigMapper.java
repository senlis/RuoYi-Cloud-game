package com.ruoyi.gserve.mapper;

import com.ruoyi.gserve.domain.GameProjectChConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏项目 ClickHouse 配置 Mapper
 * <p>
 * 仅读取 game_project 表的 project_id 和 clickhouse_config 字段。
 * 与 system 模块的 GameProjectMapper 共用同一张 MySQL 表但互不依赖。
 *
 * @author ruoyi
 */
@Mapper
public interface GameProjectChConfigMapper {

    /**
     * 根据项目 ID 查询 ClickHouse 配置
     */
    GameProjectChConfig selectById(Long projectId);
}

package com.ruoyi.bridge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 游戏服务器Mapper（仅查询 backend_url）
 * 与 system 模块共用 game_server 表
 *
 * @author ruoyi
 */
@Mapper
public interface GameServerMapper {

    /**
     * 根据服务器ID和分区CODE查询后台交互地址
     */
    @Select("SELECT gs.backend_url FROM game_server gs " +
            "JOIN game_region gr ON gs.region_id = gr.region_id " +
            "WHERE gs.server_id = #{serverId} AND gr.region_code = #{regionCode} LIMIT 1")
    String selectBackendUrl(@Param("regionCode") String regionCode, @Param("serverId") Integer serverId);
}

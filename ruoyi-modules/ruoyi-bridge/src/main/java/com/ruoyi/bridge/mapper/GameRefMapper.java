package com.ruoyi.bridge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 游戏通用引用查询 Mapper
 * <p>
 * 跨模块查询 game_channel / game_region 基础数据，供后台管理页面级联选择使用。
 *
 * @author ruoyi
 */
@Mapper
public interface GameRefMapper {

    /**
     * 查询所有启用的渠道选项 (channelCode, channelName)
     */
    @Select("SELECT channel_code AS \"channelCode\", channel_name AS \"channelName\" FROM game_channel WHERE status = '0' ORDER BY sort")
    List<Map<String, Object>> selectChannelOptions();

    /**
     * 根据渠道编码查询对应区域选项 (regionCode, regionName)
     */
    @Select("SELECT region_code AS \"regionCode\", region_name AS \"regionName\" FROM game_region WHERE channel_id = (SELECT channel_id FROM game_channel WHERE channel_code = #{channelCode}) AND status = '0' ORDER BY sort")
    List<Map<String, Object>> selectRegionOptions(@Param("channelCode") String channelCode);

    /**
     * 根据渠道编码查询项目ID
     */
    @Select("SELECT project_id FROM game_channel WHERE channel_code = #{channelCode}")
    Long selectProjectIdByChannelCode(@Param("channelCode") String channelCode);

    /**
     * 查询项目的 dynamic_fields（用于获取 md5key）
     */
    @Select("SELECT dynamic_fields FROM game_project WHERE project_id = #{projectId}")
    String selectProjectDynamicFields(@Param("projectId") Long projectId);

    /**
     * 一次性查询：channelCode → game_channel.project_id → game_project.dynamic_fields → md5key
     */
    @Select("SELECT gp.dynamic_fields FROM game_channel gc"
            + " INNER JOIN game_project gp ON gc.project_id = gp.project_id"
            + " WHERE gc.channel_code = #{channelCode}")
    String selectProjectDynamicFieldsByChannelCode(@Param("channelCode") String channelCode);
}

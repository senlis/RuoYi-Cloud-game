package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BrChannelArgConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 渠道参数配置Mapper（system模块管理用）
 *
 * @author ruoyi
 */
@Mapper
public interface BrChannelArgConfigMapper {

    @Results(id = "configResult", value = {
        @Result(property = "channelId", column = "channel_id"),
        @Result(property = "channelKey", column = "channel_key"),
        @Result(property = "regionKey", column = "region_key"),
        @Result(property = "platformName", column = "platform_name"),
        @Result(property = "packageName", column = "package_name"),
        @Result(property = "appId", column = "app_id"),
        @Result(property = "appKey", column = "app_key"),
        @Result(property = "payKey", column = "pay_key"),
        @Result(property = "authUrl", column = "auth_url"),
        @Result(property = "packageParams", column = "package_params"),
        @Result(property = "sort", column = "sort"),
        @Result(property = "status", column = "status"),
        @Result(property = "remark", column = "remark"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT * FROM br_channel_arg_config WHERE channel_id = #{channelId}")
    BrChannelArgConfig selectById(Long channelId);

    @ResultMap("configResult")
    @Select("SELECT * FROM br_channel_arg_config WHERE channel_key = #{channelKey} AND region_key = #{regionKey}")
    BrChannelArgConfig selectByKey(@Param("channelKey") String channelKey, @Param("regionKey") String regionKey);

    @ResultMap("configResult")
    @Select("<script>"
            + "SELECT * FROM br_channel_arg_config"
            + "<where>"
            + "<if test='channelKey != null and channelKey != \"\"'> AND channel_key = #{channelKey}</if>"
            + "<if test='regionKey != null and regionKey != \"\"'> AND region_key like concat('%', #{regionKey}, '%')</if>"
            + "<if test='platformName != null and platformName != \"\"'> AND platform_name like concat('%', #{platformName}, '%')</if>"
            + "<if test='status != null and status != \"\"'> AND status = #{status}</if>"
            + "</where>"
            + "ORDER BY sort ASC, channel_id ASC"
            + "</script>")
    List<BrChannelArgConfig> selectList(BrChannelArgConfig config);

    @Options(useGeneratedKeys = true, keyProperty = "channelId")
    @Insert("INSERT INTO br_channel_arg_config (channel_key, region_key, platform_name, package_name, "
            + "app_id, app_key, pay_key, auth_url, package_params, sort, status, remark, create_time) "
            + "VALUES (#{channelKey}, #{regionKey}, #{platformName}, #{packageName}, "
            + "#{appId}, #{appKey}, #{payKey}, #{authUrl}, #{packageParams}, #{sort}, #{status}, #{remark}, now())")
    int insert(BrChannelArgConfig config);

    @Update("<script>UPDATE br_channel_arg_config"
            + "<set>"
            + "<if test='platformName != null'>platform_name = #{platformName},</if>"
            + "<if test='packageName != null'>package_name = #{packageName},</if>"
            + "<if test='appId != null'>app_id = #{appId},</if>"
            + "<if test='appKey != null'>app_key = #{appKey},</if>"
            + "<if test='payKey != null'>pay_key = #{payKey},</if>"
            + "<if test='authUrl != null'>auth_url = #{authUrl},</if>"
            + "<if test='packageParams != null'>package_params = #{packageParams},</if>"
            + "<if test='sort != null'>sort = #{sort},</if>"
            + "<if test='status != null'>status = #{status},</if>"
            + "<if test='remark != null'>remark = #{remark},</if>"
            + "update_time = now()"
            + "</set>"
            + "WHERE channel_id = #{channelId}</script>")
    int update(BrChannelArgConfig config);

    @Delete("DELETE FROM br_channel_arg_config WHERE channel_id IN (${ids})")
    int deleteByIds(@Param("ids") String ids);

    // ---- 下拉选项 ----

    @Select("SELECT channel_code AS channelCode, channel_name AS channelName FROM game_channel WHERE status = '0' ORDER BY sort")
    List<Map<String, Object>> selectChannelOptions();

    @Select("SELECT region_code AS regionCode, region_name AS regionName FROM game_region WHERE channel_id = (SELECT channel_id FROM game_channel WHERE channel_code = #{channelCode}) AND status = '0' ORDER BY sort")
    List<Map<String, Object>> selectRegionOptions(String channelCode);
}

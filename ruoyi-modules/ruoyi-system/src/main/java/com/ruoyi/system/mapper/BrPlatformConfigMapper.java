package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BrPlatformConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 渠道接入平台配置Mapper（system模块管理用）
 *
 * @author ruoyi
 */
@Mapper
public interface BrPlatformConfigMapper {

    @Results(id = "platformResult", value = {
        @Result(property = "platformId", column = "platform_id"),
        @Result(property = "channelKey", column = "channel_key"),
        @Result(property = "platformName", column = "platform_name"),
        @Result(property = "status", column = "status"),
        @Result(property = "rechargeStatus", column = "recharge_status"),
        @Result(property = "dbHost", column = "db_host"),
        @Result(property = "dbPort", column = "db_port"),
        @Result(property = "dbName", column = "db_name"),
        @Result(property = "dbUser", column = "db_user"),
        @Result(property = "dbPwd", column = "db_pwd"),
        @Result(property = "remark", column = "remark"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT * FROM br_platform_config WHERE platform_id = #{platformId}")
    BrPlatformConfig selectById(Long platformId);

    @ResultMap("platformResult")
    @Select("SELECT * FROM br_platform_config WHERE channel_key = #{channelKey}")
    BrPlatformConfig selectByChannelKey(String channelKey);

    @ResultMap("platformResult")
    @Select("<script>"
            + "SELECT * FROM br_platform_config"
            + "<where>"
            + "<if test='channelKey != null and channelKey != \"\"'> AND channel_key = #{channelKey}</if>"
            + "<if test='platformName != null and platformName != \"\"'> AND platform_name like concat('%', #{platformName}, '%')</if>"
            + "<if test='status != null and status != \"\"'> AND status = #{status}</if>"
            + "</where>"
            + "ORDER BY platform_id ASC"
            + "</script>")
    List<BrPlatformConfig> selectList(BrPlatformConfig config);

    @Options(useGeneratedKeys = true, keyProperty = "platformId")
    @Insert("INSERT INTO br_platform_config (channel_key, platform_name, status, recharge_status, "
            + "db_host, db_port, db_name, db_user, db_pwd, remark, create_time) "
            + "VALUES (#{channelKey}, #{platformName}, #{status}, #{rechargeStatus}, "
            + "#{dbHost}, #{dbPort}, #{dbName}, #{dbUser}, #{dbPwd}, #{remark}, now())")
    int insert(BrPlatformConfig config);

    @Update("<script>UPDATE br_platform_config"
            + "<set>"
            + "<if test='platformName != null'>platform_name = #{platformName},</if>"
            + "<if test='status != null'>status = #{status},</if>"
            + "<if test='rechargeStatus != null'>recharge_status = #{rechargeStatus},</if>"
            + "<if test='dbHost != null'>db_host = #{dbHost},</if>"
            + "<if test='dbPort != null'>db_port = #{dbPort},</if>"
            + "<if test='dbName != null'>db_name = #{dbName},</if>"
            + "<if test='dbUser != null'>db_user = #{dbUser},</if>"
            + "<if test='dbPwd != null'>db_pwd = #{dbPwd},</if>"
            + "<if test='remark != null'>remark = #{remark},</if>"
            + "update_time = now()"
            + "</set>"
            + "WHERE platform_id = #{platformId}</script>")
    int update(BrPlatformConfig config);

    @Delete("DELETE FROM br_platform_config WHERE platform_id IN (${ids})")
    int deleteByIds(@Param("ids") String ids);
}

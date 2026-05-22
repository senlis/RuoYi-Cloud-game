package com.ruoyi.bridge.mapper;

import java.util.List;
import com.ruoyi.bridge.domain.BrPlatformConfig;
import org.apache.ibatis.annotations.*;

/**
 * 渠道接入平台配置Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface BrPlatformConfigMapper {

    /**
     * 根据渠道KEY查询平台配置
     *
     * @param channelKey 渠道KEY
     * @return 渠道接入平台配置
     */
    @Select("SELECT * FROM br_platform_config WHERE channel_key = #{channelKey}")
    @Results(id = "platformConfigResult", value = {
            @Result(column = "platform_id", property = "platformId"),
            @Result(column = "channel_key", property = "channelKey"),
            @Result(column = "platform_name", property = "platformName"),
            @Result(column = "status", property = "status"),
            @Result(column = "recharge_status", property = "rechargeStatus"),
            @Result(column = "db_host", property = "dbHost"),
            @Result(column = "db_port", property = "dbPort"),
            @Result(column = "db_name", property = "dbName"),
            @Result(column = "db_user", property = "dbUser"),
            @Result(column = "db_pwd", property = "dbPwd"),
            @Result(column = "remark", property = "remark"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    public BrPlatformConfig selectByChannelKey(String channelKey);

    /**
     * 查询渠道接入平台配置列表
     *
     * @param config 查询条件
     * @return 渠道接入平台配置集合
     */
    @Select("<script>"
            + "SELECT * FROM br_platform_config"
            + "<where>"
            + "<if test='channelKey != null and channelKey != \"\"'> AND channel_key LIKE CONCAT('%', #{channelKey}, '%')</if>"
            + "<if test='platformName != null and platformName != \"\"'> AND platform_name LIKE CONCAT('%', #{platformName}, '%')</if>"
            + "<if test='status != null and status != \"\"'> AND status = #{status}</if>"
            + "</where>"
            + " ORDER BY platform_id DESC"
            + "</script>")
    @ResultMap("platformConfigResult")
    public List<BrPlatformConfig> selectList(BrPlatformConfig config);

    /**
     * 新增渠道接入平台配置
     *
     * @param config 渠道接入平台配置
     * @return 结果
     */
    @Insert("INSERT INTO br_platform_config (channel_key, platform_name, status, recharge_status, "
            + "db_host, db_port, db_name, db_user, db_pwd, remark, create_time, update_time) "
            + "VALUES (#{channelKey}, #{platformName}, #{status}, #{rechargeStatus}, "
            + "#{dbHost}, #{dbPort}, #{dbName}, #{dbUser}, #{dbPwd}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "platformId")
    public int insert(BrPlatformConfig config);

    /**
     * 修改渠道接入平台配置
     *
     * @param config 渠道接入平台配置
     * @return 结果
     */
    @Update("<script>"
            + "UPDATE br_platform_config"
            + "<set>"
            + "<if test='channelKey != null'>channel_key = #{channelKey},</if>"
            + "<if test='platformName != null'>platform_name = #{platformName},</if>"
            + "<if test='status != null'>status = #{status},</if>"
            + "<if test='rechargeStatus != null'>recharge_status = #{rechargeStatus},</if>"
            + "<if test='dbHost != null'>db_host = #{dbHost},</if>"
            + "<if test='dbPort != null'>db_port = #{dbPort},</if>"
            + "<if test='dbName != null'>db_name = #{dbName},</if>"
            + "<if test='dbUser != null'>db_user = #{dbUser},</if>"
            + "<if test='dbPwd != null'>db_pwd = #{dbPwd},</if>"
            + "<if test='remark != null'>remark = #{remark},</if>"
            + "update_time = NOW()"
            + "</set>"
            + "WHERE platform_id = #{platformId}"
            + "</script>")
    public int update(BrPlatformConfig config);

    /**
     * 根据平台ID查询配置
     *
     * @param platformId 平台ID
     * @return 渠道接入平台配置
     */
    @Select("SELECT * FROM br_platform_config WHERE platform_id = #{platformId}")
    @ResultMap("platformConfigResult")
    public BrPlatformConfig selectById(Long platformId);

    /**
     * 批量删除渠道接入平台配置
     *
     * @param platformIds 需要删除的平台ID
     * @return 结果
     */
    @Delete("<script>"
            + "DELETE FROM br_platform_config WHERE platform_id IN "
            + "<foreach item='platformId' collection='array' open='(' separator=',' close=')'>#{platformId}</foreach>"
            + "</script>")
    public int deleteByIds(Long[] platformIds);

    /**
     * 查询所有已启用的平台配置
     *
     * @return 已启用的平台配置集合
     */
    @Select("SELECT * FROM br_platform_config WHERE status = '0'")
    @ResultMap("platformConfigResult")
    public List<BrPlatformConfig> selectAllEnabled();
}

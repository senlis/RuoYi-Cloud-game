package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BrRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色信息Mapper接口（system模块管理用）
 * <p>
 * 查询渠道分库中的 br_role 表数据。
 *
 * @author ruoyi
 */
@Mapper
public interface BrRoleMapper {

    @Results(id = "roleResult", value = {
        @Result(property = "playerId", column = "player_id"),
        @Result(property = "channelKey", column = "channel_key"),
        @Result(property = "regionCode", column = "region_code"),
        @Result(property = "serverId", column = "server_id"),
        @Result(property = "accountId", column = "account_id"),
        @Result(property = "roleName", column = "role_name"),
        @Result(property = "level", column = "level"),
        @Result(property = "vocation", column = "vocation"),
        @Result(property = "vip", column = "vip"),
        @Result(property = "fight", column = "fight"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    @Select("<script>"
            + "SELECT player_id, channel_key, region_code, server_id, account_id, "
            + "       role_name, level, vocation, vip, fight, "
            + "       create_time, update_time"
            + "  FROM br_role"
            + "<where>"
            + "<if test='channelKey != null and channelKey != \"\"'> AND channel_key = #{channelKey}</if>"
            + "<if test='playerId != null'> AND player_id = #{playerId}</if>"
            + "<if test='roleName != null and roleName != \"\"'> AND role_name LIKE concat('%', #{roleName}, '%')</if>"
            + "<if test='accountId != null and accountId != \"\"'> AND account_id = #{accountId}</if>"
            + "<if test='serverId != null'> AND server_id = #{serverId}</if>"
            + "<if test='serverIds != null and serverIds != \"\"'> AND FIND_IN_SET(server_id, #{serverIds})</if>"
            + "</where>"
            + "ORDER BY player_id ASC"
            + "</script>")
    List<BrRole> selectList(BrRole role);
}

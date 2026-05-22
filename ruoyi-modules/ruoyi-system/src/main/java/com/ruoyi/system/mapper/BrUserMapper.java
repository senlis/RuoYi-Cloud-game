package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BrUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 渠道用户Mapper接口（system模块管理用）
 * <p>
 * 查询渠道分库中的 br_user 表数据。
 *
 * @author ruoyi
 */
@Mapper
public interface BrUserMapper {

    @Results(id = "userResult", value = {
        @Result(property = "userId", column = "user_id"),
        @Result(property = "identityId", column = "identity_id"),
        @Result(property = "channelKey", column = "channel_key"),
        @Result(property = "channelUserId", column = "channel_user_id"),
        @Result(property = "serverId", column = "server_id"),
        @Result(property = "playerId", column = "player_id"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "lastLoginTime", column = "last_login_time")
    })
    @Select("<script>"
            + "SELECT user_id, identity_id, channel_key, channel_user_id, "
            + "       server_id, player_id, create_time, last_login_time"
            + "  FROM br_user"
            + "<where>"
            + "<if test='channelKey != null and channelKey != \"\"'> AND channel_key = #{channelKey}</if>"
            + "<if test='identityId != null and identityId != \"\"'> AND identity_id = #{identityId}</if>"
            + "<if test='channelUserId != null and channelUserId != \"\"'> AND channel_user_id = #{channelUserId}</if>"
            + "<if test='serverId != null'> AND server_id = #{serverId}</if>"
            + "<if test='serverIds != null and serverIds != \"\"'> AND FIND_IN_SET(server_id, #{serverIds})</if>"
            + "</where>"
            + "ORDER BY user_id ASC"
            + "</script>")
    List<BrUser> selectList(BrUser user);
}

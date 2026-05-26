package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.GameResourceRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 内部资源申请Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface GameResourceRequestMapper {

    @Results(id = "requestResult", value = {
        @Result(property = "requestId", column = "request_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "requestType", column = "request_type"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "serverIds", column = "server_ids"),
        @Result(property = "channelIds", column = "channel_ids"),
        @Result(property = "regionIds", column = "region_ids"),
        @Result(property = "playerIds", column = "player_ids"),
        @Result(property = "resources", column = "resources"),
        @Result(property = "reason", column = "reason"),
        @Result(property = "urgency", column = "urgency"),
        @Result(property = "status", column = "status"),
        @Result(property = "applicant", column = "applicant"),
        @Result(property = "approver", column = "approver"),
        @Result(property = "auditRemark", column = "audit_remark"),
        @Result(property = "failedServerIds", column = "failed_server_ids"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT * FROM t_gm_resource_request WHERE request_id = #{requestId}")
    GameResourceRequest selectById(Long requestId);

    @ResultMap("requestResult")
    @Select("<script>"
            + "SELECT * FROM t_gm_resource_request"
            + "<where>"
            + "<if test='requestId != null'> AND request_id = #{requestId}</if>"
            + "<if test='title != null and title != \"\"'> AND title like concat('%', #{title}, '%')</if>"
            + "<if test='requestType != null'> AND request_type = #{requestType}</if>"
            + "<if test='status != null'> AND status = #{status}</if>"
            + "<if test='applicant != null and applicant != \"\"'> AND applicant = #{applicant}</if>"
            + "<if test='beginTime != null and beginTime != \"\"'> AND created_at &gt;= #{beginTime}</if>"
            + "<if test='endTime != null and endTime != \"\"'> AND created_at &lt;= #{endTime}</if>"
            + "</where>"
            + "ORDER BY created_at DESC"
            + "</script>")
    List<GameResourceRequest> selectList(GameResourceRequest req);

    @Insert("INSERT INTO t_gm_resource_request (title, request_type, project_id, server_ids, channel_ids, region_ids, player_ids,"
            + " resources, reason, urgency, status, applicant, approver, created_at)"
            + " VALUES (#{title}, #{requestType}, #{projectId}, #{serverIds}, #{channelIds}, #{regionIds}, #{playerIds},"
            + " #{resources}, #{reason}, #{urgency}, #{status}, #{applicant}, #{approver}, now())")
    @Options(useGeneratedKeys = true, keyProperty = "requestId")
    int insert(GameResourceRequest req);

    @Update("<script>UPDATE t_gm_resource_request"
            + "<set>"
            + "<if test='title != null'>title = #{title},</if>"
            + "<if test='requestType != null'>request_type = #{requestType},</if>"
            + "<if test='serverIds != null'>server_ids = #{serverIds},</if>"
            + "<if test='channelIds != null'>channel_ids = #{channelIds},</if>"
            + "<if test='regionIds != null'>region_ids = #{regionIds},</if>"
            + "<if test='playerIds != null'>player_ids = #{playerIds},</if>"
            + "<if test='resources != null'>resources = #{resources},</if>"
            + "<if test='reason != null'>reason = #{reason},</if>"
            + "<if test='urgency != null'>urgency = #{urgency},</if>"
            + "<if test='status != null'>status = #{status},</if>"
            + "<if test='approver != null'>approver = #{approver},</if>"
            + "<if test='auditRemark != null'>audit_remark = #{auditRemark},</if>"
            + "<if test='failedServerIds != null'>failed_server_ids = #{failedServerIds},</if>"
            + "updated_at = now()"
            + "</set>"
            + "WHERE request_id = #{requestId}</script>")
    int update(GameResourceRequest req);

    @Delete("DELETE FROM t_gm_resource_request WHERE request_id = #{requestId}")
    int deleteById(Long requestId);

    /** 查询有审批权限且有权访问指定项目的用户 */
    @Select("SELECT DISTINCT u.user_name, u.nick_name FROM sys_user u"
            + " INNER JOIN sys_user_role ur ON u.user_id = ur.user_id"
            + " WHERE ur.role_id IN ("
            + "   SELECT rm.role_id FROM sys_role_menu rm"
            + "   INNER JOIN sys_menu m ON rm.menu_id = m.menu_id"
            + "   WHERE m.perms = 'gm:resource:audit')"
            + " AND (ur.role_id = 1"  // admin 角色总是有权限
            + "   OR ur.role_id IN (SELECT gra.role_id FROM game_role_auth gra"
            + "     WHERE gra.entity_type = 'project' AND gra.entity_id = #{projectId}))"
            + " ORDER BY u.user_name ASC")
    List<Map<String, Object>> selectApprovers(@Param("projectId") Long projectId);
}

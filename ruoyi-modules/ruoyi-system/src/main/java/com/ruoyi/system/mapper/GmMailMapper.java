package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.GmMail;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * GM 邮件Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface GmMailMapper {

    @Results(id = "mailResult", value = {
        @Result(property = "mailId", column = "mail_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "sendType", column = "send_type"),
        @Result(property = "sendTime", column = "send_time"),
        @Result(property = "expireDays", column = "expire_days"),
        @Result(property = "targetType", column = "target_type"),
        @Result(property = "targetPlayers", column = "target_players"),
        @Result(property = "minLevel", column = "min_level"),
        @Result(property = "maxLevel", column = "max_level"),
        @Result(property = "minVip", column = "min_vip"),
        @Result(property = "maxVip", column = "max_vip"),
        @Result(property = "rewards", column = "rewards"),
        @Result(property = "status", column = "status"),
        @Result(property = "auditRemark", column = "audit_remark"),
        @Result(property = "serverIds", column = "server_ids"),
        @Result(property = "failedServerIds", column = "failed_server_ids"),
        @Result(property = "channelIds", column = "channel_ids"),
        @Result(property = "regionIds", column = "region_ids"),
        @Result(property = "projectId", column = "project_id"),
        @Result(property = "projectName", column = "project_name"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT m.*, gp.project_name FROM t_gm_mail m"
            + " LEFT JOIN game_project gp ON m.project_id = gp.project_id"
            + " WHERE m.mail_id = #{mailId}")
    GmMail selectById(Long mailId);

    @ResultMap("mailResult")
    @Select("<script>"
            + "SELECT m.*, gp.project_name FROM t_gm_mail m"
            + " LEFT JOIN game_project gp ON m.project_id = gp.project_id"
            + "<where>"
            + "<if test='mailId != null'> AND m.mail_id = #{mailId}</if>"
            + "<if test='title != null and title != \"\"'> AND m.title like concat('%', #{title}, '%')</if>"
            + "<if test='status != null'> AND m.status = #{status}</if>"
            + "<if test='projectId != null'> AND m.project_id = #{projectId}</if>"
            + "<if test='beginTime != null and beginTime != \"\"'> AND m.created_at &gt;= #{beginTime}</if>"
            + "<if test='endTime != null and endTime != \"\"'> AND m.created_at &lt;= #{endTime}</if>"
            + "<if test='params.authProjectIds != null and params.authProjectIds.size() > 0'>"
            + " AND m.project_id IN "
            + "<foreach item='id' collection='params.authProjectIds' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</if>"
            + "</where>"
            + "ORDER BY created_at DESC"
            + "</script>")
    List<GmMail> selectList(GmMail mail);

    @Insert("INSERT INTO t_gm_mail (title, content, send_type, send_time, expire_days,"
            + " target_type, target_players, min_level, max_level, min_vip, max_vip,"
            + " rewards, status, server_ids, failed_server_ids, channel_ids, region_ids, project_id, created_by, created_at)"
            + " VALUES (#{title}, #{content}, #{sendType}, #{sendTime}, #{expireDays},"
            + " #{targetType}, #{targetPlayers}, #{minLevel}, #{maxLevel}, #{minVip}, #{maxVip},"
            + " #{rewards}, #{status}, #{serverIds}, #{failedServerIds}, #{channelIds}, #{regionIds}, #{projectId}, #{createdBy}, now())")
    @Options(useGeneratedKeys = true, keyProperty = "mailId")
    int insert(GmMail mail);

    @Update("<script>UPDATE t_gm_mail"
            + "<set>"
            + "<if test='title != null'>title = #{title},</if>"
            + "<if test='content != null'>content = #{content},</if>"
            + "<if test='sendType != null'>send_type = #{sendType},</if>"
            + "<if test='sendTime != null'>send_time = #{sendTime},</if>"
            + "<if test='expireDays != null'>expire_days = #{expireDays},</if>"
            + "<if test='targetType != null'>target_type = #{targetType},</if>"
            + "<if test='targetPlayers != null'>target_players = #{targetPlayers},</if>"
            + "<if test='minLevel != null'>min_level = #{minLevel},</if>"
            + "<if test='maxLevel != null'>max_level = #{maxLevel},</if>"
            + "<if test='minVip != null'>min_vip = #{minVip},</if>"
            + "<if test='maxVip != null'>max_vip = #{maxVip},</if>"
            + "<if test='rewards != null'>rewards = #{rewards},</if>"
            + "<if test='status != null'>status = #{status},</if>"
            + "<if test='auditRemark != null'>audit_remark = #{auditRemark},</if>"
            + "<if test='serverIds != null'>server_ids = #{serverIds},</if>"
            + "<if test='failedServerIds != null'>failed_server_ids = #{failedServerIds},</if>"
            + "<if test='channelIds != null'>channel_ids = #{channelIds},</if>"
            + "<if test='regionIds != null'>region_ids = #{regionIds},</if>"
            + "updated_at = now()"
            + "</set>"
            + "WHERE mail_id = #{mailId}</script>")
    int update(GmMail mail);

    @Delete("DELETE FROM t_gm_mail WHERE mail_id = #{mailId}")
    int deleteById(Long mailId);
}

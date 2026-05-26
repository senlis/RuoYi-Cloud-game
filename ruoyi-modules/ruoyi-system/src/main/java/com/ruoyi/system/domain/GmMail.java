package com.ruoyi.system.domain;

import com.ruoyi.common.core.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * GM 邮件对象 t_gm_mail
 * <p>
 * 用于向玩家发送通知邮件或奖励邮件，支持富文本、定时发送、条件筛选、道具奖励。
 *
 * @author ruoyi
 */
public class GmMail {

    private static final long serialVersionUID = 1L;

    /** 邮件ID */
    private Long mailId;

    /** 邮件标题 */
    @Excel(name = "邮件标题")
    private String title;

    /** 邮件内容（富文本HTML） */
    private String content;

    /** 发送方式 0=立即 1=定时 */
    @Excel(name = "发送方式", readConverterExp = "0=立即,1=定时")
    private Integer sendType;

    /** 定时发送时间(UTC) */
    @Excel(name = "发送时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /** 有效期天数 */
    @Excel(name = "有效期(天)")
    private Integer expireDays;

    /** 目标范围 0=全服 1=条件筛选 2=指定玩家 */
    @Excel(name = "目标范围", readConverterExp = "0=全服,1=条件筛选,2=指定玩家")
    private Integer targetType;

    /** 指定角色ID列表(逗号分隔) */
    private String targetPlayers;

    /** 最低等级 */
    @Excel(name = "最低等级")
    private Integer minLevel;

    /** 最高等级 */
    @Excel(name = "最高等级")
    private Integer maxLevel;

    /** 最低VIP */
    @Excel(name = "最低VIP")
    private Integer minVip;

    /** 最高VIP */
    @Excel(name = "最高VIP")
    private Integer maxVip;

    /** 附件奖励JSON [{itemId,itemName,count}] */
    private String rewards;

    /** 状态 0=待审核 1=审核通过 2=已发送 3=发送失败 4=驳回 5=已撤回 */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=审核通过,2=已发送,3=发送失败,4=驳回,5=已撤回")
    private Integer status;

    /** 驳回原因 */
    private String auditRemark;

    /** 项目名称（关联查询，非持久化） */
    private String projectName;

    /** 目标服务器ID列表(逗号分隔) */
    private String serverIds;

    /** 推送失败服务器ID列表(逗号分隔) */
    private String failedServerIds;

    /** 选中渠道ID列表(逗号分隔) */
    private String channelIds;

    /** 选中分区ID列表(逗号分隔) */
    private String regionIds;

    /** 所属项目 */
    private Long projectId;

    /** 创建人 */
    @Excel(name = "创建人")
    private String createdBy;

    /** 创建时间 */
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 查询参数（用于权限过滤等） */
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if (params == null) { params = new HashMap<>(); }
        return params;
    }

    public void setParams(Map<String, Object> params) { this.params = params; }

    // ---- 非持久化查询辅助字段 ----

    /** 开始时间（查询用） */
    private String beginTime;

    /** 结束时间（查询用） */
    private String endTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("mailId", getMailId())
                .append("title", getTitle())
                .append("sendType", getSendType())
                .append("sendTime", getSendTime())
                .append("status", getStatus())
                .append("createdAt", getCreatedAt())
                .toString();
    }

    public Long getMailId() { return mailId; }
    public void setMailId(Long mailId) { this.mailId = mailId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getSendType() { return sendType; }
    public void setSendType(Integer sendType) { this.sendType = sendType; }
    public Date getSendTime() { return sendTime; }
    public void setSendTime(Date sendTime) { this.sendTime = sendTime; }
    public Integer getExpireDays() { return expireDays; }
    public void setExpireDays(Integer expireDays) { this.expireDays = expireDays; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public String getTargetPlayers() { return targetPlayers; }
    public void setTargetPlayers(String targetPlayers) { this.targetPlayers = targetPlayers; }
    public Integer getMinLevel() { return minLevel; }
    public void setMinLevel(Integer minLevel) { this.minLevel = minLevel; }
    public Integer getMaxLevel() { return maxLevel; }
    public void setMaxLevel(Integer maxLevel) { this.maxLevel = maxLevel; }
    public Integer getMinVip() { return minVip; }
    public void setMinVip(Integer minVip) { this.minVip = minVip; }
    public Integer getMaxVip() { return maxVip; }
    public void setMaxVip(Integer maxVip) { this.maxVip = maxVip; }
    public String getRewards() { return rewards; }
    public void setRewards(String rewards) { this.rewards = rewards; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getServerIds() { return serverIds; }
    public void setServerIds(String serverIds) { this.serverIds = serverIds; }
    public String getFailedServerIds() { return failedServerIds; }
    public void setFailedServerIds(String failedServerIds) { this.failedServerIds = failedServerIds; }
    public String getChannelIds() { return channelIds; }
    public void setChannelIds(String channelIds) { this.channelIds = channelIds; }
    public String getRegionIds() { return regionIds; }
    public void setRegionIds(String regionIds) { this.regionIds = regionIds; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}

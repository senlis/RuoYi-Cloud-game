package com.ruoyi.system.domain;

import com.ruoyi.common.core.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 内部资源申请对象 t_gm_resource_request
 *
 * @author ruoyi
 */
public class GameResourceRequest {

    private static final long serialVersionUID = 1L;

    /** 申请单ID */
    private Long requestId;

    /** 申请标题 */
    @Excel(name = "申请标题")
    private String title;

    /** 申请类型 0=道具 1=货币 2=等级 3=创建角色 4=自定义 */
    @Excel(name = "申请类型", readConverterExp = "0=道具,1=货币,2=等级,3=创建角色,4=自定义")
    private Integer requestType;

    /** 项目ID */
    private Long projectId;

    /** 目标服务器ID列表 */
    private String serverIds;

    /** 选中渠道ID列表 */
    private String channelIds;

    /** 选中分区ID列表 */
    private String regionIds;

    /** 目标角色ID列表 */
    @Excel(name = "目标角色")
    private String playerIds;

    /** 资源内容JSON */
    private String resources;

    /** 申请理由 */
    @Excel(name = "申请理由")
    private String reason;

    /** 紧急程度 0=普通 1=紧急 */
    @Excel(name = "紧急程度", readConverterExp = "0=普通,1=紧急")
    private Integer urgency;

    /** 状态 0=待审批 1=审批通过 2=已发放 3=发放失败 4=驳回 5=已撤回 */
    @Excel(name = "状态", readConverterExp = "0=待审批,1=审批通过,2=已发放,3=发放失败,4=驳回,5=已撤回")
    private Integer status;

    /** 申请人 */
    @Excel(name = "申请人")
    private String applicant;

    /** 审批人 */
    @Excel(name = "审批人")
    private String approver;

    /** 审批意见 */
    private String auditRemark;

    /** 发放失败服务器ID列表 */
    private String failedServerIds;

    /** 创建时间 */
    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 查询参数 */
    private Map<String, Object> params;

    /** 开始时间（查询用） */
    private String beginTime;

    /** 结束时间（查询用） */
    private String endTime;

    public Map<String, Object> getParams() {
        if (params == null) params = new HashMap<>();
        return params;
    }
    public void setParams(Map<String, Object> params) { this.params = params; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("requestId", getRequestId())
                .append("title", getTitle())
                .append("status", getStatus())
                .append("applicant", getApplicant())
                .append("createdAt", getCreatedAt())
                .toString();
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getRequestType() { return requestType; }
    public void setRequestType(Integer requestType) { this.requestType = requestType; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getServerIds() { return serverIds; }
    public void setServerIds(String serverIds) { this.serverIds = serverIds; }
    public String getChannelIds() { return channelIds; }
    public void setChannelIds(String channelIds) { this.channelIds = channelIds; }
    public String getRegionIds() { return regionIds; }
    public void setRegionIds(String regionIds) { this.regionIds = regionIds; }
    public String getPlayerIds() { return playerIds; }
    public void setPlayerIds(String playerIds) { this.playerIds = playerIds; }
    public String getResources() { return resources; }
    public void setResources(String resources) { this.resources = resources; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getUrgency() { return urgency; }
    public void setUrgency(Integer urgency) { this.urgency = urgency; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getApplicant() { return applicant; }
    public void setApplicant(String applicant) { this.applicant = applicant; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getFailedServerIds() { return failedServerIds; }
    public void setFailedServerIds(String failedServerIds) { this.failedServerIds = failedServerIds; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}

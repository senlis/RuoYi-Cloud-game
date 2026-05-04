package com.ruoyi.system.domain;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 游戏渠道对象 game_channel
 *
 * @author ruoyi
 */
public class GameChannel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 渠道ID */
    @Excel(name = "渠道ID")
    private Long channelId;

    /** 所属项目ID */
    @Excel(name = "所属项目")
    private Long projectId;

    /** 渠道编码 */
    @Excel(name = "渠道编码")
    private String channelCode;

    /** 渠道名称 */
    @Excel(name = "渠道名称")
    private String channelName;

    /** 渠道类型(0=官方 1=联运 2=海外) */
    @Excel(name = "渠道类型", readConverterExp = "0=官方,1=联运,2=海外")
    private String channelType;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 动态字段值(JSON) */
    private String dynamicFields;

    /** 项目名称(用于列表显示) */
    @Excel(name = "所属项目名称")
    private String projectName;

    public Long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(Long channelId)
    {
        this.channelId = channelId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    @NotBlank(message = "渠道编码不能为空")
    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    @NotBlank(message = "渠道名称不能为空")
    public String getChannelName()
    {
        return channelName;
    }

    public void setChannelName(String channelName)
    {
        this.channelName = channelName;
    }

    public String getChannelType()
    {
        return channelType;
    }

    public void setChannelType(String channelType)
    {
        this.channelType = channelType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getContactPerson()
    {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson)
    {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    @JsonIgnore
    public String getDynamicFields()
    {
        return dynamicFields;
    }

    @JsonProperty("dynamicFields")
    public Object getDynamicFieldsObj()
    {
        if (dynamicFields == null || dynamicFields.isEmpty())
        {
            return new java.util.HashMap<String, Object>();
        }
        try
        {
            return JSON.parseObject(dynamicFields);
        }
        catch (Exception e)
        {
            return dynamicFields;
        }
    }

    public void setDynamicFields(Object dynamicFields)
    {
        if (dynamicFields == null)
        {
            this.dynamicFields = null;
        }
        else if (dynamicFields instanceof String)
        {
            this.dynamicFields = (String) dynamicFields;
        }
        else
        {
            this.dynamicFields = JSON.toJSONString(dynamicFields);
        }
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("channelId", getChannelId())
            .append("projectId", getProjectId())
            .append("channelCode", getChannelCode())
            .append("channelName", getChannelName())
            .append("channelType", getChannelType())
            .append("status", getStatus())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("sort", getSort())
            .append("dynamicFields", getDynamicFields())
            .append("projectName", getProjectName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

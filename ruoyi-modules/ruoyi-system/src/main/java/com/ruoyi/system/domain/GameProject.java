package com.ruoyi.system.domain;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 游戏项目对象 game_project
 *
 * @author ruoyi
 */
public class GameProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @Excel(name = "项目ID", cellType = ColumnType.NUMERIC)
    private Long projectId;

    /** 项目编码 */
    @Excel(name = "项目编码")
    private String projectCode;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 动态字段值(JSON) */
    private String dynamicFields;

    /** ClickHouse 连接配置(JSON) — 在项目管理页面配置 */
    private String clickhouseConfig;

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

    @JsonIgnore
    public String getClickhouseConfig()
    {
        return clickhouseConfig;
    }

    @JsonProperty("clickhouseConfig")
    public Object getClickhouseConfigObj()
    {
        if (clickhouseConfig == null || clickhouseConfig.isEmpty())
        {
            return new java.util.HashMap<String, Object>();
        }
        try
        {
            return JSON.parseObject(clickhouseConfig);
        }
        catch (Exception e)
        {
            return clickhouseConfig;
        }
    }

    @JsonProperty("clickhouseConfig")
    public void setClickhouseConfig(Object clickhouseConfig)
    {
        if (clickhouseConfig == null)
        {
            this.clickhouseConfig = null;
        }
        else if (clickhouseConfig instanceof String)
        {
            this.clickhouseConfig = (String) clickhouseConfig;
        }
        else
        {
            this.clickhouseConfig = JSON.toJSONString(clickhouseConfig);
        }
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    @NotBlank(message = "项目编码不能为空")
    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    @NotBlank(message = "项目名称不能为空")
    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("projectId", getProjectId())
            .append("projectCode", getProjectCode())
            .append("projectName", getProjectName())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("dynamicFields", getDynamicFields())
            .append("clickhouseConfig", getClickhouseConfig())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

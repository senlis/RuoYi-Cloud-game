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
 * 游戏分区对象 game_region
 *
 * @author ruoyi
 */
public class GameRegion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分区ID */
    @Excel(name = "分区ID")
    private Long regionId;

    /** 所属渠道ID */
    @Excel(name = "所属渠道ID")
    private Long channelId;

    /** 所属项目ID(冗余) */
    @Excel(name = "所属项目ID")
    private Long projectId;

    /** 分区编码 */
    @Excel(name = "分区编码")
    private String regionCode;

    /** 分区名称 */
    @Excel(name = "分区名称")
    private String regionName;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 动态字段值(JSON) */
    private String dynamicFields;

    /** 导出配置(JSON) -- 存储服务器数组JSON */
    private String servers;

    /** 代理分区key */
    @Excel(name = "代理分区key")
    private String proxyRegionKey;

    /** 分区配置JSON(region级别导出字段) */
    private String config;

    /** 项目名称(用于列表显示) */
    @Excel(name = "所属项目")
    private String projectName;

    /** 代理分区名称(用于列表显示) */
    @Excel(name = "代理分区")
    private String proxyRegionName;

    /** 渠道名称(用于列表显示) */
    @Excel(name = "所属渠道")
    private String channelName;

    public Long getRegionId()
    {
        return regionId;
    }

    public void setRegionId(Long regionId)
    {
        this.regionId = regionId;
    }

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

    @NotBlank(message = "分区编码不能为空")
    public String getRegionCode()
    {
        return regionCode;
    }

    public void setRegionCode(String regionCode)
    {
        this.regionCode = regionCode;
    }

    @NotBlank(message = "分区名称不能为空")
    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
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

    public String getServers()
    {
        return servers;
    }

    public void setServers(String servers)
    {
        this.servers = servers;
    }

    public String getProxyRegionKey()
    {
        return proxyRegionKey;
    }

    public void setProxyRegionKey(String proxyRegionKey)
    {
        this.proxyRegionKey = proxyRegionKey;
    }

    public String getConfig()
    {
        return config;
    }

    public void setConfig(String config)
    {
        this.config = config;
    }

    public String getProxyRegionName()
    {
        return proxyRegionName;
    }

    public void setProxyRegionName(String proxyRegionName)
    {
        this.proxyRegionName = proxyRegionName;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getChannelName()
    {
        return channelName;
    }

    public void setChannelName(String channelName)
    {
        this.channelName = channelName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("regionId", getRegionId())
            .append("channelId", getChannelId())
            .append("projectId", getProjectId())
            .append("regionCode", getRegionCode())
            .append("regionName", getRegionName())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("dynamicFields", getDynamicFields())
            .append("servers", getServers())
            .append("proxyRegionKey", getProxyRegionKey())
            .append("proxyRegionName", getProxyRegionName())
            .append("config", getConfig())
            .append("projectName", getProjectName())
            .append("channelName", getChannelName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

package com.ruoyi.system.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 游戏服务器对象 game_server
 *
 * @author ruoyi
 */
public class GameServer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    @Excel(name = "ID")
    private Long id;

    /** 所属分区ID */
    @Excel(name = "所属分区ID")
    private Long regionId;

    /** 服务器ID */
    @Excel(name = "服务器ID")
    private Integer serverId;

    /** 服务器名称 */
    @Excel(name = "服务器名称")
    private String serverName;

    /** 服务器类型(0=正式 1=测试 2=体验) */
    @Excel(name = "服务器类型", readConverterExp = "0=正式,1=测试,2=体验")
    private String serverType;

    /** 状态(0=正常 1=维护 2=停服) */
    @Excel(name = "状态", readConverterExp = "0=初始,1=待开,2=停服,3=维护,4=正常")
    private String status;

    /** 开服时间 */
    @Excel(name = "开服时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date openTime;

    /** 后台交互地址 */
    @Excel(name = "后台地址")
    private String backendUrl;

    /** 服务器地址 */
    @Excel(name = "服务器地址")
    private String serverAddress;

    /** 端口 */
    @Excel(name = "端口")
    private Integer port;

    /** 部署路径 */
    @Excel(name = "部署路径")
    private String deployPath;

    /** 游戏数据库配置(JSON) */
    private String gameDbConfig;

    /** 游戏日志数据库配置(JSON) */
    private String logDbConfig;

    /** 当前版本号 */
    @Excel(name = "当前版本")
    private String currentVersion;

    /** 合服母服ID */
    @Excel(name = "合服母服ID")
    private Integer mergeParentId;

    /** 合服母服名称(用于列表显示) */
    private String mergeParentName;

    /** 合服信息(JSON数组) */
    private String mergeInfo;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 动态字段值(JSON) */
    private String dynamicFields;

    /** 分区名称(用于列表显示) */
    @Excel(name = "所属分区")
    private String regionName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getRegionId()
    {
        return regionId;
    }

    public void setRegionId(Long regionId)
    {
        this.regionId = regionId;
    }

    @NotNull(message = "服务器ID不能为空")
    public Integer getServerId()
    {
        return serverId;
    }

    public void setServerId(Integer serverId)
    {
        this.serverId = serverId;
    }

    @NotBlank(message = "服务器名称不能为空")
    public String getServerName()
    {
        return serverName;
    }

    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    public String getServerType()
    {
        return serverType;
    }

    public void setServerType(String serverType)
    {
        this.serverType = serverType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getOpenTime()
    {
        return openTime;
    }

    public void setOpenTime(Date openTime)
    {
        this.openTime = openTime;
    }

    public String getBackendUrl()
    {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl)
    {
        this.backendUrl = backendUrl;
    }

    public String getServerAddress()
    {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress)
    {
        this.serverAddress = serverAddress;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getDeployPath()
    {
        return deployPath;
    }

    public void setDeployPath(String deployPath)
    {
        this.deployPath = deployPath;
    }

    public String getGameDbConfig()
    {
        return gameDbConfig;
    }

    public void setGameDbConfig(String gameDbConfig)
    {
        this.gameDbConfig = gameDbConfig;
    }

    public String getLogDbConfig()
    {
        return logDbConfig;
    }

    public void setLogDbConfig(String logDbConfig)
    {
        this.logDbConfig = logDbConfig;
    }

    public String getCurrentVersion()
    {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion)
    {
        this.currentVersion = currentVersion;
    }

    public Integer getMergeParentId()
    {
        return mergeParentId;
    }

    public void setMergeParentId(Integer mergeParentId)
    {
        this.mergeParentId = mergeParentId;
    }

    public String getMergeParentName()
    {
        return mergeParentName;
    }

    public void setMergeParentName(String mergeParentName)
    {
        this.mergeParentName = mergeParentName;
    }

    public String getMergeInfo()
    {
        return mergeInfo;
    }

    public void setMergeInfo(String mergeInfo)
    {
        this.mergeInfo = mergeInfo;
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

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("serverId", getServerId())
            .append("regionId", getRegionId())
            .append("serverName", getServerName())
            .append("serverType", getServerType())
            .append("status", getStatus())
            .append("openTime", getOpenTime())
            .append("backendUrl", getBackendUrl())
            .append("serverAddress", getServerAddress())
            .append("port", getPort())
            .append("deployPath", getDeployPath())
            .append("gameDbConfig", getGameDbConfig())
            .append("logDbConfig", getLogDbConfig())
            .append("currentVersion", getCurrentVersion())
            .append("mergeParentId", getMergeParentId())
            .append("mergeParentName", getMergeParentName())
            .append("mergeInfo", getMergeInfo())
            .append("sort", getSort())
            .append("dynamicFields", getDynamicFields())
            .append("regionName", getRegionName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

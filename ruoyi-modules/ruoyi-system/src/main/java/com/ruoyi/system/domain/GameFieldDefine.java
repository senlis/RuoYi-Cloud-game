package com.ruoyi.system.domain;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 动态字段定义对象 game_field_define
 *
 * @author ruoyi
 */
public class GameFieldDefine extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 字段ID */
    @Excel(name = "字段ID")
    private Long fieldId;

    /** 所属实体(project/channel/region/server) */
    @Excel(name = "所属实体")
    private String entityType;

    /** 字段显示名称 */
    @Excel(name = "字段显示名称")
    private String fieldLabel;

    /** 字段编码 */
    @Excel(name = "字段编码")
    private String fieldCode;

    /** 字段类型(text/number/textarea/date/select/boolean) */
    @Excel(name = "字段类型")
    private String fieldType;

    /** 选项配置(JSON数组) */
    private String fieldOptions;

    /** 是否导出(Y/N) */
    @Excel(name = "是否导出", readConverterExp = "Y=是,N=否")
    private String isExport;

    /** 是否必填 */
    private String isRequired;

    /** 默认值 */
    private String defaultValue;

    /** 排序号 */
    @Excel(name = "排序号")
    private Integer sort;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(Long fieldId)
    {
        this.fieldId = fieldId;
    }

    @NotBlank(message = "所属实体不能为空")
    public String getEntityType()
    {
        return entityType;
    }

    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }

    @NotBlank(message = "字段显示名称不能为空")
    public String getFieldLabel()
    {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel)
    {
        this.fieldLabel = fieldLabel;
    }

    @NotBlank(message = "字段编码不能为空")
    public String getFieldCode()
    {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode)
    {
        this.fieldCode = fieldCode;
    }

    @NotBlank(message = "字段类型不能为空")
    public String getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    @JsonIgnore
    public String getFieldOptions()
    {
        return fieldOptions;
    }

    @JsonProperty("fieldOptions")
    public Object getFieldOptionsObj()
    {
        if (fieldOptions == null || fieldOptions.isEmpty())
        {
            return new java.util.ArrayList<Object>();
        }
        try
        {
            return JSON.parseArray(fieldOptions);
        }
        catch (Exception e)
        {
            return fieldOptions;
        }
    }

    public void setFieldOptions(Object fieldOptions)
    {
        if (fieldOptions == null)
        {
            this.fieldOptions = null;
        }
        else if (fieldOptions instanceof String)
        {
            this.fieldOptions = (String) fieldOptions;
        }
        else
        {
            this.fieldOptions = JSON.toJSONString(fieldOptions);
        }
    }

    public String getIsExport()
    {
        return isExport;
    }

    public void setIsExport(String isExport)
    {
        this.isExport = isExport;
    }

    public String getIsRequired()
    {
        return isRequired;
    }

    public void setIsRequired(String isRequired)
    {
        this.isRequired = isRequired;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("fieldId", getFieldId())
            .append("entityType", getEntityType())
            .append("fieldLabel", getFieldLabel())
            .append("fieldCode", getFieldCode())
            .append("fieldType", getFieldType())
            .append("fieldOptions", getFieldOptions())
            .append("isExport", getIsExport())
            .append("isRequired", getIsRequired())
            .append("defaultValue", getDefaultValue())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

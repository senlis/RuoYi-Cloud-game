package com.ruoyi.system.domain;

import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

/**
 * 事件类型配置对象 game_event_type_config
 * <p>
 * 定义 game_event_role_event 表中 event_type 的元信息，
 * 包括 n1~n10 / s1~s5 各字段的参数语义、标签和枚举值映射。
 *
 * @author ruoyi
 */
public class GameEventTypeConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 事件类型编码 */
    @Excel(name = "事件类型")
    private String eventType;

    /** 事件中文名 */
    @Excel(name = "事件名称")
    private String eventName;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 参数解析定义(JSON) */
    private String paramDefine;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotBlank(message = "事件类型编码不能为空")
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @NotBlank(message = "事件名称不能为空")
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParamDefine() {
        return paramDefine;
    }

    public void setParamDefine(String paramDefine) {
        this.paramDefine = paramDefine;
    }
}

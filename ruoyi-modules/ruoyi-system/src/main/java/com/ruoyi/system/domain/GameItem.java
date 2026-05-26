package com.ruoyi.system.domain;

import com.ruoyi.common.core.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 游戏道具配置对象 t_item_config
 * <p>
 * 通过 Excel 导入道具 ID 和名称，按项目隔离。
 * 用于道具发放、GM 邮件等功能中的快捷选择。
 *
 * @author ruoyi
 */
public class GameItem {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 道具ID（Excel 第一列） */
    @Excel(name = "id")
    private Long itemId;

    /** 道具名称（Excel 第二列） */
    @Excel(name = "name")
    private String itemName;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("projectId", getProjectId())
                .append("itemId", getItemId())
                .append("itemName", getItemName())
                .append("createdAt", getCreatedAt())
                .append("updatedAt", getUpdatedAt())
                .toString();
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}

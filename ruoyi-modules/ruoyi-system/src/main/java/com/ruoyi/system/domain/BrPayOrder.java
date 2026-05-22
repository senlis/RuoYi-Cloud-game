package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 支付订单表 br_pay_order
 * <p>
 * 按渠道分库存储，每个渠道库独立建表。
 * 记录玩家充值订单信息。
 *
 * @author ruoyi
 */
public class BrPayOrder {

    private static final long serialVersionUID = 1L;

    /** 订单ID（业务主键） */
    @Excel(name = "订单ID")
    private String orderId;

    /** 渠道订单ID */
    private String channelOrderId;

    /** 渠道标识 */
    @Excel(name = "渠道KEY")
    private String channelKey;

    /** 分区CODE */
    private String regionCode;

    /** 服务器ID */
    @Excel(name = "服务器ID")
    private Integer serverId;

    /** 服务器ID列表（多选查询用，逗号分隔） */
    private String serverIds;

    /** 游戏角色ID */
    @Excel(name = "角色ID")
    private Long playerId;

    /** 账号ID（来自 br_role 关联） */
    @Excel(name = "账号ID")
    private String accountId;

    /** 玩家名（来自 br_role 关联） */
    @Excel(name = "玩家名")
    private String roleName;

    /** 用户标识 */
    private String userId;

    /** 平台身份标识 */
    private String identityId;

    /** 商品ID */
    private String productId;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String productName;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 游戏币数量 */
    @Excel(name = "游戏币")
    private Integer gameCurrency;

    /** 货币类型 */
    private String currency;

    /** 支付状态（0初始 5确认兑换中 10兑换成功 15补单成功 20兑换失败） */
    @Excel(name = "支付状态")
    private Integer payStatus;

    /** 兑换地址 */
    private String exchangeUrl;

    /** 支付方式 */
    @Excel(name = "支付方式")
    private String payWay;

    /** 设备信息 */
    private String equipment;

    /** 备注 */
    private String remark;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "下单时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 开始时间（查询用） */
    private String beginTime;

    /** 结束时间（查询用） */
    private String endTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("orderId", getOrderId())
                .append("channelOrderId", getChannelOrderId())
                .append("channelKey", getChannelKey())
                .append("regionCode", getRegionCode())
                .append("serverId", getServerId())
                .append("serverIds", getServerIds())
                .append("playerId", getPlayerId())
                .append("accountId", getAccountId())
                .append("roleName", getRoleName())
                .append("userId", getUserId())
                .append("identityId", getIdentityId())
                .append("productId", getProductId())
                .append("productName", getProductName())
                .append("amount", getAmount())
                .append("gameCurrency", getGameCurrency())
                .append("currency", getCurrency())
                .append("payStatus", getPayStatus())
                .append("exchangeUrl", getExchangeUrl())
                .append("payWay", getPayWay())
                .append("equipment", getEquipment())
                .append("remark", getRemark())
                .append("orderTime", getOrderTime())
                .append("payTime", getPayTime())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getChannelOrderId() {
        return channelOrderId;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getServerIds() {
        return serverIds;
    }

    public void setServerIds(String serverIds) {
        this.serverIds = serverIds;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getGameCurrency() {
        return gameCurrency;
    }

    public void setGameCurrency(Integer gameCurrency) {
        this.gameCurrency = gameCurrency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public String getExchangeUrl() {
        return exchangeUrl;
    }

    public void setExchangeUrl(String exchangeUrl) {
        this.exchangeUrl = exchangeUrl;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

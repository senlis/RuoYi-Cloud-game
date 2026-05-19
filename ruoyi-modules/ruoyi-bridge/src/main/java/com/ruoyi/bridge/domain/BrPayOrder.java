package com.ruoyi.bridge.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 支付订单表 br_pay_order
 *
 * @author ruoyi
 */
public class BrPayOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单ID（业务主键） */
    private String orderId;

    /** 渠道订单ID */
    private String channelOrderId;

    /** 渠道标识 */
    private String channelKey;

    /** 服务器ID */
    private Integer serverId;

    /** 分区CODE */
    private String regionCode;

    /** 游戏角色ID */
    private Long playerId;

    /** 游戏角色名 */
    private String playerName;

    /** 用户标识 */
    private String userId;

    /** 平台身份标识 */
    private String identityId;

    /** 商品ID */
    private String productId;

    /** 商品名称 */
    private String productName;

    /** 金额 */
    private BigDecimal amount;

    /** 游戏币数量 */
    private Integer gameCurrency;

    /** 货币类型 */
    private String currency;

    /** 支付状态（0初始 5确认兑换中 10兑换成功 15补单成功 20兑换失败） */
    private Integer payStatus;

    /** 兑换地址 */
    private String exchangeUrl;

    /** 支付方式 */
    private String payWay;

    /** 设备信息 */
    private String equipment;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    @NotBlank
    private String sign;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("orderId", getOrderId())
                .append("channelOrderId", getChannelOrderId())
                .append("channelKey", getChannelKey())
                .append("serverId", getServerId())
                .append("playerId", getPlayerId())
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

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
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

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

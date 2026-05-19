package com.ruoyi.bridge.domain.dto;

import java.math.BigDecimal;

/**
 * 支付回调通知DTO
 *
 * @author ruoyi
 */
public class PayNotifyDTO {

    /** 渠道标识 */
    private String channelKey;

    /** 订单ID */
    private String orderId;

    /** 渠道订单ID */
    private String channelOrderId;

    /** 商品ID */
    private String productId;

    /** 金额 */
    private BigDecimal amount;

    /** 货币类型 */
    private String currency;

    /** 签名 */
    private String sign;

    /** 原始数据（完整回调JSON，用于验签等） */
    private String rawData;

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
}

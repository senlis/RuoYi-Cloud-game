package com.ruoyi.bridge.domain.dto;

/**
 * 兑换结果DTO
 *
 * @author ruoyi
 */
public class ExchangeResult {

    /** 是否成功 */
    private boolean success;

    /** 消息 */
    private String message;

    /** 订单ID */
    private String orderId;

    public ExchangeResult() {
    }

    public ExchangeResult(boolean success, String message, String orderId) {
        this.success = success;
        this.message = message;
        this.orderId = orderId;
    }

    /**
     * 创建成功结果
     *
     * @param orderId 订单ID
     * @return 兑换结果
     */
    public static ExchangeResult ok(String orderId) {
        return new ExchangeResult(true, "兑换成功", orderId);
    }

    /**
     * 创建失败结果
     *
     * @param message 失败原因
     * @return 兑换结果
     */
    public static ExchangeResult fail(String message) {
        return new ExchangeResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}

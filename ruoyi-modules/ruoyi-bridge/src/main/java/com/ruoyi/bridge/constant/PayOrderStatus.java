package com.ruoyi.bridge.constant;

/**
 * 支付订单状态枚举
 *
 * @author ruoyi
 */
public enum PayOrderStatus {

    /** 初始状态 */
    ORDER_INIT(0, "初始"),

    /** 已确认，兑换中 */
    ORDER_CONFIRMED_EXCHANGING(5, "已确认-兑换中"),

    /** 兑换成功 */
    ORDER_EXCHANGED_SUCCESS(10, "兑换成功"),

    /** 补单兑换成功 */
    ORDER_RESCUE_EXCHANGED_SUCCESS(15, "补单成功"),

    /** 兑换失败 */
    ORDER_EXCHANGE_FAILED(20, "兑换失败");

    private final int code;
    private final String description;

    PayOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举实例，未找到返回null
     */
    public static PayOrderStatus fromCode(int code) {
        for (PayOrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(code);
    }
}

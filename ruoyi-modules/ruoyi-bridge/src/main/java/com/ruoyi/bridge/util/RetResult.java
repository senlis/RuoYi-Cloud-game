package com.ruoyi.bridge.util;

/**
 * 通用返回结果封装类
 *
 * @param <T> 数据类型
 * @author ruoyi
 */
public class RetResult<T> {

    /** 成功状态码 */
    public static final int SUCCESS_CODE = 200;

    /** 失败状态码 */
    public static final int FAIL_CODE = 500;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;

    public RetResult() {
    }

    public RetResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> RetResult<T> ok(T data) {
        return new RetResult<>(SUCCESS_CODE, "success", data);
    }

    /**
     * 创建成功结果
     *
     * @param msg  消息
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> RetResult<T> ok(String msg, T data) {
        return new RetResult<>(SUCCESS_CODE, msg, data);
    }

    /**
     * 创建失败结果
     *
     * @param msg 失败消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> RetResult<T> fail(String msg) {
        return new RetResult<>(FAIL_CODE, msg, null);
    }

    /**
     * 创建失败结果
     *
     * @param code 状态码
     * @param msg  失败消息
     * @param <T>  数据类型
     * @return 失败结果
     */
    public static <T> RetResult<T> fail(int code, String msg) {
        return new RetResult<>(code, msg, null);
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

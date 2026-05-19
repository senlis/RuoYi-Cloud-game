package com.ruoyi.bridge.service;

import java.util.List;
import com.ruoyi.bridge.domain.BrPayOrder;
import com.ruoyi.bridge.domain.dto.ExchangeResult;

/**
 * 支付订单Service接口
 *
 * @author ruoyi
 */
public interface IBrPayOrderService {

    /**
     * 查询订单
     *
     * @param orderId 订单ID
     * @return 支付订单
     */
    public BrPayOrder selectByOrderId(String orderId);

    /**
     * 根据渠道标识+渠道订单ID查询订单
     *
     * @param channelKey     渠道标识
     * @param channelOrderId 渠道订单ID
     * @return 支付订单
     */
    public BrPayOrder selectByChannelOrder(String channelKey, String channelOrderId);

    /**
     * 创建订单
     *
     * @param payOrder 支付订单
     * @return 结果
     */
    public int createOrder(BrPayOrder payOrder);

    /**
     * 更新订单
     *
     * @param payOrder 支付订单
     * @return 结果
     */
    public int updateOrder(BrPayOrder payOrder);

    /**
     * 处理支付回调
     *
     * @param payOrder 支付订单（需包含渠道信息）
     * @return 兑换结果
     */
    public ExchangeResult processPayment(BrPayOrder payOrder);

    /**
     * 查询订单列表
     *
     * @param payOrder 查询条件
     * @return 订单集合
     */
    public List<BrPayOrder> selectList(BrPayOrder payOrder);

    /**
     * 批量删除订单
     *
     * @param orderIds 需要删除的订单ID
     * @return 结果
     */
    public int deleteByIds(String[] orderIds);
}

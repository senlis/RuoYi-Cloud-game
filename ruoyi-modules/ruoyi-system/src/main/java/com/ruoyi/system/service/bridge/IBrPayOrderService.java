package com.ruoyi.system.service.bridge;

import com.ruoyi.system.domain.BrPayOrder;

import java.util.List;

/**
 * 支付订单Service接口
 * <p>
 * 查询渠道分库中的 br_pay_order 表数据，自动切换数据源到对应渠道数据库。
 *
 * @author ruoyi
 */
public interface IBrPayOrderService {

    /**
     * 查询支付订单列表
     *
     * @param channelKey 渠道KEY（用于路由到对应渠道数据库）
     * @param payOrder   查询条件
     * @return 支付订单列表
     */
    List<BrPayOrder> selectPayOrderList(String channelKey, BrPayOrder payOrder);
}

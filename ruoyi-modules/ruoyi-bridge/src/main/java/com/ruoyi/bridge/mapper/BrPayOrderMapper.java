package com.ruoyi.bridge.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bridge.domain.BrPayOrder;

/**
 * 支付订单Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface BrPayOrderMapper {

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
    public BrPayOrder selectByChannelOrder(@Param("channelKey") String channelKey,
                                           @Param("channelOrderId") String channelOrderId);

    /**
     * 查询订单列表
     *
     * @param payOrder 查询条件
     * @return 订单集合
     */
    public List<BrPayOrder> selectList(BrPayOrder payOrder);

    /**
     * 新增订单
     *
     * @param payOrder 支付订单
     * @return 结果
     */
    public int insert(BrPayOrder payOrder);

    /**
     * 修改订单
     *
     * @param payOrder 支付订单
     * @return 结果
     */
    public int update(BrPayOrder payOrder);

    /**
     * 批量删除订单
     *
     * @param orderIds 需要删除的订单ID
     * @return 结果
     */
    public int deleteByIds(String[] orderIds);
}

package com.ruoyi.system.service.bridge.impl;

import com.ruoyi.system.datasource.ChannelDs;
import com.ruoyi.system.domain.BrPayOrder;
import com.ruoyi.system.mapper.BrPayOrderMapper;
import com.ruoyi.system.service.bridge.IBrPayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付订单Service实现
 * <p>
 * 通过 @ChannelDs 自动切换数据源到对应渠道的独立数据库。
 *
 * @author ruoyi
 */
@Service
public class BrPayOrderServiceImpl implements IBrPayOrderService {

    @Autowired
    private BrPayOrderMapper brPayOrderMapper;

    @ChannelDs("#channelKey")
    @Override
    public List<BrPayOrder> selectPayOrderList(String channelKey, BrPayOrder payOrder) {
        payOrder.setChannelKey(channelKey);
        return brPayOrderMapper.selectList(payOrder);
    }
}

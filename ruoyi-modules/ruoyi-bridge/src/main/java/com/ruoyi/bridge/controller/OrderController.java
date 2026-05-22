package com.ruoyi.bridge.controller;

import com.ruoyi.bridge.constant.PayOrderStatus;
import com.ruoyi.bridge.datasource.ChannelDs;
import com.ruoyi.bridge.domain.BrPayOrder;
import com.ruoyi.bridge.domain.OrderResult;
import com.ruoyi.bridge.domain.dto.ExchangeResult;
import com.ruoyi.bridge.mapper.GameServerMapper;
import com.ruoyi.bridge.pay.PayOrderRequest;
import com.ruoyi.bridge.config.ConfigConstant;
import com.ruoyi.bridge.service.IBrPayOrderService;
import com.ruoyi.bridge.service.impl.BrPayOrderServiceImpl;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.MD5;
import com.ruoyi.common.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器
 * <p>
 * 预创建订单 → 根据 serverId 自动填充 exchangeUrl → 返回 orderId
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/bridge/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IBrPayOrderService orderService;

    @Autowired
    private BrPayOrderServiceImpl orderServiceImpl;

    @Autowired
    private GameServerMapper gameServerMapper;

    /**
     * 订单参数校验 （统一公共使用）
     *
     * @author hj
     * @since 2015年9月7日14:56:24
     */
    public R<OrderResult> creatOrderValidate(long playerId, String identityId, int serverId, String price, String sign) {
        if (playerId <= 0 || StringUtils.isBlank(price) || StringUtils.isBlank(sign)) {
            log.error("参数非法：" + " ;  playerId =  " + playerId + "; price: " + price + "; sign: " + sign);
            return R.fail("参数校验错误!创建订单失败!");
        }

        if (new BigDecimal(price).compareTo(BigDecimal.ZERO) < 0) {
            return R.fail("参数校验错误!创建订单失败!");
        }

        String md5Str = MD5.digest(playerId + identityId + serverId + price + ConfigConstant.CLIENT_KEY);
        if (!md5Str.equals(sign)) {
            log.error("MD5参数校验错误!" + md5Str + "<>" + " ;  playerId =  " + playerId + "; price: " + price + "; sign: " + sign);
            return R.fail("秘钥参数校验错误!创建订单失败!");
        }
        return R.ok();
    }

    /**
     * 预创建订单 — 玩家支付前调用
     * 自动根据 serverId 从 game_server 获取 backend_url 填充 exchangeUrl
     */
    @ChannelDs("#req.channelKey")
    @PostMapping("/preCreate")
    public R<OrderResult> preCreateOrder(@RequestBody PayOrderRequest req) {
        R<OrderResult> orderResult = creatOrderValidate(req.getPlayerId(), req.getIdentityId(), req.getServerId(), req.getPrice(), req.getSign());
        if (R.isError(orderResult)) {
            return orderResult;
        }
        log.info("预创建订单: productId={}, amount={}, serverId={}",
                req.getRefId(), req.getPrice(), req.getServerId());
        // 从 game_server 获取后台交互地址作为 exchangeUrl
        if (StringUtils.isBlank(req.getServerUrl())) {
            String regionCode = req.getRegionCode();
            String backendUrl = gameServerMapper.selectBackendUrl(regionCode, req.getServerId());
            if (StringUtils.isBlank(backendUrl)) {
                log.warn("服务器 backend_url 未配置: serverId={}", req.getServerId());
                return R.fail("服务器地址未配置");
            }
            req.setExchangeUrl(backendUrl);
        }

        try {
            return orderServiceImpl.createOrderBeforePay(req);
        } catch (Exception e) {
            log.error("预创建订单异常", e);
            return R.fail("创建订单失败：" + e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    public R<BrPayOrder> getOrder(@PathVariable("orderId") String orderId) {
        BrPayOrder order = orderService.selectByOrderId(orderId);
        if (order == null) return R.fail("订单不存在");
        return R.ok(order);
    }

    @PostMapping("/{orderId}/rescue")
    public R<BrPayOrder> rescueOrder(@PathVariable("orderId") String orderId) {
        BrPayOrder order = orderService.selectByOrderId(orderId);
        if (order == null) return R.fail("订单不存在");
        int status = order.getPayStatus() != null ? order.getPayStatus() : -1;
        boolean canRescue = status == PayOrderStatus.ORDER_EXCHANGE_FAILED.getCode()
                || status == PayOrderStatus.ORDER_INIT.getCode();
        if (!canRescue) {
            return status >= PayOrderStatus.ORDER_EXCHANGED_SUCCESS.getCode()
                    ? R.fail("订单已完成，无需补单")
                    : R.fail("当前订单状态不允许补单: status=" + status);
        }
        ExchangeResult ex = orderServiceImpl.exchangeGameMoneyByRescue(order, 1);
        if (ex.isSuccess()) {
            order.setPayStatus(PayOrderStatus.ORDER_RESCUE_EXCHANGED_SUCCESS.getCode());
            order.setRemark("补单成功");
            orderService.updateOrder(order);
            return R.ok(order);
        }
        return R.fail("补单失败：" + ex.getMessage());
    }
}

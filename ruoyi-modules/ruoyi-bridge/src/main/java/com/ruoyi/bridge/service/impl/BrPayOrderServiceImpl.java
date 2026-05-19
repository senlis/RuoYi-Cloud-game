package com.ruoyi.bridge.service.impl;

import com.ruoyi.bridge.constant.PayOrderStatus;
import com.ruoyi.bridge.domain.BrPayOrder;
import com.ruoyi.bridge.domain.OrderResult;
import com.ruoyi.bridge.domain.dto.ExchangeResult;
import com.ruoyi.bridge.mapper.BrPayOrderMapper;
import com.ruoyi.bridge.service.IBrPayOrderService;
import com.ruoyi.bridge.config.ConfigConstant;
import com.ruoyi.bridge.util.BridgeHttpHelper;
import com.ruoyi.bridge.util.CryptoUtil;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.DebugUtil;
import com.ruoyi.common.core.utils.MD5;
import com.ruoyi.common.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * 支付订单Service实现
 * 包含订单预创建、游戏币兑换（调用游戏服exchangeUrl）、补单等完整业务逻辑
 *
 * @author ruoyi
 */
@Service
public class BrPayOrderServiceImpl implements IBrPayOrderService {

    private static final Logger log = LoggerFactory.getLogger(BrPayOrderServiceImpl.class);

    @Value("${bridge.exchange.rate:10}")
    private int exchangeRate;

    @Value("${bridge.game.key:UyVUos02DBzGTSUh}")
    private String gameKey;

    @Autowired
    private BrPayOrderMapper brPayOrderMapper;

    @Autowired
    private BridgeHttpHelper httpHelper;

    @Override
    public BrPayOrder selectByOrderId(String orderId) {
        return brPayOrderMapper.selectByOrderId(orderId);
    }

    @Override
    public BrPayOrder selectByChannelOrder(String channelKey, String channelOrderId) {
        return brPayOrderMapper.selectByChannelOrder(channelKey, channelOrderId);
    }

    @Override
    public int createOrder(BrPayOrder payOrder) {
        return brPayOrderMapper.insert(payOrder);
    }

    @Override
    public int updateOrder(BrPayOrder payOrder) {
        return brPayOrderMapper.update(payOrder);
    }

    private void updateOrderStatus(BrPayOrder order, int status, String statusDesc) {
        order.setPayStatus(status);
        order.setRemark(statusDesc);
        brPayOrderMapper.update(order);
    }

    /**
     * 预创建订单（接收 PayOrderRequest 参数）
     */
    public R<OrderResult> createOrderBeforePay(com.ruoyi.bridge.pay.PayOrderRequest req) {
        BrPayOrder order = new BrPayOrder();
        order.setPlayerId(req.getPlayerId());
        order.setPlayerName(req.getPlayerName());
        order.setUserId(req.getChannelUserId());
        order.setIdentityId(req.getIdentityId());
        order.setChannelKey(req.getChannelKey());
        order.setServerId(req.getServerId());
        order.setProductId(req.getRefId());
        order.setAmount(new BigDecimal(req.getPrice()));
        order.setExchangeUrl(req.getServerUrl());
        order.setPayWay(req.getPKey());
        order.setEquipment(req.getOs());
        order.setRemark(req.getOther());
        return createOrderBeforePay(order, null);
    }

    /**
     * 在玩家支付之前预创建订单（统一公共使用）
     *
     * @param order
     * @return
     */
    public R<OrderResult> createOrderBeforePay(BrPayOrder order) {
        try {
            // 创建订单
            final BrPayOrder payOrder = preCreateOrder(order, null);
            log.info("---->预创建订单成功，订单信息是：<----" + payOrder.toString());
            String sign = MD5.digest(String.valueOf(payOrder.getPlayerId()), payOrder.getUserId(), payOrder.getOrderId(), gameKey);
            OrderResult result = new OrderResult(payOrder.getPlayerId(), payOrder.getUserId(), payOrder.getOrderId(), sign, payOrder.getOrderId());
            return R.ok(result);
        } catch (Exception e) {
            log.error("订单创建失败，请检查相关信息！" + DebugUtil.printStack(e));
            return R.fail("桥接平台内部出错!!");
        }
    }

    /**
     * 在玩家支付之前预创建订单（统一公共使用）
     *
     * @param order
     * @return
     */
    public R<OrderResult> createOrderBeforePay(BrPayOrder order, String channelOrderId) {
        try {
            // 创建订单
            final BrPayOrder payOrder = preCreateOrder(order, channelOrderId);
            log.info("---->预创建订单成功，订单信息是：<----" + payOrder.toString());
            String sign = MD5.digest(String.valueOf(payOrder.getPlayerId()), payOrder.getUserId(), payOrder.getOrderId(), gameKey);
            OrderResult result = new OrderResult(payOrder.getPlayerId(), payOrder.getUserId(), payOrder.getOrderId(), sign, payOrder.getOrderId());
            return R.ok(result);
        } catch (Exception e) {
            log.error("订单创建失败，请检查相关信息！" + DebugUtil.printStack(e));
            return R.fail("桥接平台内部出错!!");
        }
    }

    /**
     * 预创建订单 — 玩家支付前调用，生成系统订单号并计算游戏币
     */
    public BrPayOrder preCreateOrder(BrPayOrder req,String channelOrderId) {
        BrPayOrder order = new BrPayOrder();
        String orderId = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        order.setOrderId(orderId);
        order.setChannelOrderId(channelOrderId);
        order.setPayStatus(PayOrderStatus.ORDER_INIT.getCode());
        order.setChannelKey(req.getChannelKey());
        order.setServerId(req.getServerId());
        order.setPlayerId(req.getPlayerId());
        order.setUserId(req.getUserId());
        order.setIdentityId(req.getIdentityId());
        order.setProductId(req.getProductId());
        order.setProductName(req.getProductName());
        order.setAmount(req.getAmount());
        order.setCurrency(req.getCurrency());
        order.setExchangeUrl(req.getExchangeUrl());
        order.setPayWay(req.getPayWay());
        order.setEquipment(req.getEquipment());
        order.setRemark(req.getRemark());
        if (req.getAmount() != null && exchangeRate > 0) {
            order.setGameCurrency(req.getAmount().multiply(BigDecimal.valueOf(exchangeRate)).intValue());
        }
        order.setOrderTime(new Timestamp(System.currentTimeMillis()));
        brPayOrderMapper.insert(order);
        log.info("预创建订单成功: orderId={}, amount={}, gameCurrency={}", orderId, req.getAmount(), order.getGameCurrency());
        return order;
    }

    /**
     * 兑换游戏币 — 向游戏服发送兑换请求
     * <p>
     * 签名规则: MD5(identityName + playerName + gameGold + amount + gameKey)
     * 请求地址: exchangeUrl + "/services"
     */
    public ExchangeResult exchangeGameMoney(BrPayOrder payOrder, String refId, int moneyType) {
        String identityName = payOrder.getIdentityId() != null ? payOrder.getIdentityId() : "";
        String playerName = payOrder.getPlayerName() != null ? payOrder.getPlayerName() : "";
        String gameGold = String.valueOf(payOrder.getGameCurrency() != null ? payOrder.getGameCurrency() : 0);
        String amountStr = payOrder.getAmount() != null ? payOrder.getAmount().toPlainString() : "0";
        String signSrc = identityName + playerName + gameGold + amountStr + gameKey;
        String sign = CryptoUtil.md5(signSrc);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("identityName", identityName);
        params.put("playerId", String.valueOf(payOrder.getPlayerId() != null ? payOrder.getPlayerId() : 0));
        params.put("playerName", CryptoUtil.base64Encode(playerName));
        params.put("gameGold", gameGold);
        params.put("payMoney", amountStr);
        params.put("payTime", String.valueOf(payOrder.getPayTime() != null ? payOrder.getPayTime().getTime() : System.currentTimeMillis()));
        params.put("payWay", payOrder.getPayWay() != null ? payOrder.getPayWay() : "");
        params.put("cpOrderId", payOrder.getChannelOrderId() != null ? payOrder.getChannelOrderId() : "");
        params.put("os", payOrder.getEquipment() != null ? payOrder.getEquipment() : "");
        params.put("remarks", payOrder.getRemark() != null ? payOrder.getRemark() : "");
        params.put("refId", refId != null ? refId : "");
        params.put("moneyType", String.valueOf(moneyType));
        params.put("sign", sign);
        params.put("action", "pay");

        String exchangeUrl = payOrder.getExchangeUrl();
        if (StringUtils.isBlank(exchangeUrl)) {
            log.error("兑换失败: exchangeUrl为空, orderId={}", payOrder.getOrderId());
            updateOrderStatus(payOrder, PayOrderStatus.ORDER_EXCHANGE_FAILED.getCode(), "游戏服地址未配置");
            return ExchangeResult.fail("游戏服地址未配置");
        }

        log.info("兑换请求: url={}/services, orderId={}, gameGold={}, sign={}",
                exchangeUrl, payOrder.getOrderId(), gameGold, sign);
        log.debug("兑换参数: {}", params);

        String respStr = httpHelper.doPost(exchangeUrl + "/services", params);
        if (StringUtils.isBlank(respStr)) {
            log.error("兑换失败: 游戏服无响应, orderId={}", payOrder.getOrderId());
            updateOrderStatus(payOrder, PayOrderStatus.ORDER_EXCHANGE_FAILED.getCode(), "游戏服无响应");
            return ExchangeResult.fail("游戏服连接异常，无响应结果");
        }

        log.info("兑换响应: orderId={}, resp={}", payOrder.getOrderId(), respStr);
        try {
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(respStr);
            int code = json.getIntValue("code");
            String msg = json.getString("description");
            if (code != 0) {
                log.warn("游戏服发放游戏币失败: orderId={}, code={}, msg={}", payOrder.getOrderId(), code, msg);
                updateOrderStatus(payOrder, PayOrderStatus.ORDER_EXCHANGE_FAILED.getCode(), "游戏服发放失败:" + msg);
                return ExchangeResult.fail(msg);
            }
            updateOrderStatus(payOrder, PayOrderStatus.ORDER_EXCHANGED_SUCCESS.getCode(), "兑换成功");
            log.info("兑换游戏币成功: orderId={}, gameGold={}", payOrder.getOrderId(), gameGold);
            return ExchangeResult.ok(payOrder.getOrderId());
        } catch (Exception e) {
            log.error("解析游戏服响应异常: orderId={}", payOrder.getOrderId(), e);
            updateOrderStatus(payOrder, PayOrderStatus.ORDER_EXCHANGE_FAILED.getCode(), "响应解析失败");
            return ExchangeResult.fail("响应解析失败");
        }
    }

    @Override
    public ExchangeResult processPayment(BrPayOrder payOrder) {
        if (payOrder == null || payOrder.getOrderId() == null) {
            return ExchangeResult.fail("订单不存在");
        }
        return exchangeGameMoney(payOrder, payOrder.getProductId(), 1);
    }

    public ExchangeResult exchangeGameMoneyByRescue(BrPayOrder payOrder, int moneyType) {
        return exchangeGameMoney(payOrder, payOrder.getProductId(), moneyType);
    }

    @Override
    public List<BrPayOrder> selectList(BrPayOrder payOrder) {
        return brPayOrderMapper.selectList(payOrder);
    }

    @Override
    public int deleteByIds(String[] orderIds) {
        return brPayOrderMapper.deleteByIds(orderIds);
    }
}

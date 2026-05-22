package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BrPayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 支付订单Mapper接口（system模块管理用）
 * <p>
 * 查询渠道分库中的 br_pay_order 表数据。
 *
 * @author ruoyi
 */
@Mapper
public interface BrPayOrderMapper {

    @Results(id = "payOrderResult", value = {
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "channelOrderId", column = "channel_order_id"),
        @Result(property = "channelKey", column = "channel_key"),
        @Result(property = "regionCode", column = "region_code"),
        @Result(property = "serverId", column = "server_id"),
        @Result(property = "playerId", column = "player_id"),
        @Result(property = "accountId", column = "account_id"),
        @Result(property = "roleName", column = "role_name"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "identityId", column = "identity_id"),
        @Result(property = "productId", column = "product_id"),
        @Result(property = "productName", column = "product_name"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "gameCurrency", column = "game_currency"),
        @Result(property = "currency", column = "currency"),
        @Result(property = "payStatus", column = "pay_status"),
        @Result(property = "exchangeUrl", column = "exchange_url"),
        @Result(property = "payWay", column = "pay_way"),
        @Result(property = "equipment", column = "equipment"),
        @Result(property = "remark", column = "remark"),
        @Result(property = "orderTime", column = "order_time"),
        @Result(property = "payTime", column = "pay_time"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    @Select("<script>"
            + "SELECT po.order_id, po.channel_order_id, po.channel_key, po.region_code, po.server_id, "
            + "       po.player_id, r.account_id, r.role_name, "
            + "       po.user_id, po.identity_id, po.product_id, po.product_name, "
            + "       po.amount, po.game_currency, po.currency, po.pay_status, po.exchange_url, "
            + "       po.pay_way, po.equipment, po.remark, po.order_time, po.pay_time, "
            + "       po.create_time, po.update_time"
            + "  FROM br_pay_order po"
            + "  LEFT JOIN br_role r ON po.player_id = r.player_id"
            + "<where>"
            + "<if test='channelKey != null and channelKey != \"\"'> AND po.channel_key = #{channelKey}</if>"
            + "<if test='orderId != null and orderId != \"\"'> AND po.order_id = #{orderId}</if>"
            + "<if test='serverId != null'> AND po.server_id = #{serverId}</if>"
            + "<if test='serverIds != null and serverIds != \"\"'> AND FIND_IN_SET(po.server_id, #{serverIds})</if>"
            + "<if test='payStatus != null'> AND po.pay_status = #{payStatus}</if>"
            + "<if test='beginTime != null and beginTime != \"\"'> AND po.create_time &gt;= #{beginTime}</if>"
            + "<if test='endTime != null and endTime != \"\"'> AND po.create_time &lt;= #{endTime}</if>"
            + "</where>"
            + "ORDER BY po.order_time DESC"
            + "</script>")
    List<BrPayOrder> selectList(BrPayOrder payOrder);
}

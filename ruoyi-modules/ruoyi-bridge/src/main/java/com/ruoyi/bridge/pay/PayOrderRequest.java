package com.ruoyi.bridge.pay;

import lombok.Data;

public @Data class PayOrderRequest {
	/** 角色id */
	private long playerId;
	/** 游戏账号id */
	private String identityId;
	/** 角色名 */
	private String playerName;
	/** 渠道用户id */
	private String channelUserId;
	/** 渠道key */
	private String channelKey;
	/** 渠道id */
	private int channelId;
	/** 分区KEY */
	private String pKey;
	/** 服务器id */
	private int serverId;
	/** 分区CODE */
	private String regionCode;
	/** 金额价格 */
	private String price;
	/** 系统 */
	private String os;
	/** 签名 */
	private String sign;
	/** 其他信息 */
	private String other;
	/** 游戏服兑换地址 */
	private String exchangeUrl;
	/** url */
	private String serverUrl;
	/** refId（productId） */
	private String refId;
}

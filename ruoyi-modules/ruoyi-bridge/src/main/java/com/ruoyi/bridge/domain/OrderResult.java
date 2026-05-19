package com.ruoyi.bridge.domain;

import lombok.Data;

public @Data class OrderResult {

	private long playerId;
	private String channelUserId;
	private String orderId;
	private String sign;
	private String callbackInfo;
	private String extend;

	public OrderResult(long playerId, String channelUserId, String orderId, String sign, String callbackInfo) {
		super();
		this.playerId = playerId;
		this.channelUserId = channelUserId;
		this.orderId = orderId;
		this.sign = sign;
		this.callbackInfo = callbackInfo;
	}

}

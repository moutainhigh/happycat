package com.woniu.sncp.pay.core.transfer.model;

public class TransferModel {//RECEIVE
	private String tradeOrderNo;//交易订单号
	private String receiveOrderNo;//转账单号或流水号
	private String orderNo;//
	private long platformId;//平台ID
	private String money;//转账金额
	private String reason;//转账原因
	private String clientIp;//客户端ip
	private String account;//收款帐号
	private String accountInfo;//收款姓名
	private String backendUrl;//收款回调地址
	private long merchantId;//申请号
	
	public String getReceiveOrderNo() {
		return receiveOrderNo;
	}
	public void setReceiveOrderNo(String receiveOrderNo) {
		this.receiveOrderNo = receiveOrderNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getAccountInfo() {
		return accountInfo;
	}
	public void setAccountInfo(String accountInfo) {
		this.accountInfo = accountInfo;
	}
	public String getBackendUrl() {
		return backendUrl;
	}
	public void setBackendUrl(String backendUrl) {
		this.backendUrl = backendUrl;
	}
	public long getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(long merchantId) {
		this.merchantId = merchantId;
	}
	public long getPlatformId() {
		return platformId;
	}
	public void setPlatformId(long platformId) {
		this.platformId = platformId;
	}
	public String getTradeOrderNo() {
		return tradeOrderNo;
	}
	public void setTradeOrderNo(String tradeOrderNo) {
		this.tradeOrderNo = tradeOrderNo;
	}
}

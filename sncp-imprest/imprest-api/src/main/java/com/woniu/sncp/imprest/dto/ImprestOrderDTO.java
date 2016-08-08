package com.woniu.sncp.imprest.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 充值订单
 * @author chenyx
 *
 */
public class ImprestOrderDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 支付状态
	 * @author chenyx
	 *
	 */
	public enum PaymentState {
		CREATED,PAYED,FAILED
	}
	
	/**
	 * 充值状态
	 * @author chenyx
	 *
	 */
	public enum ImprestState {
		NOT_COMPLETED,COMPLETED
	}
	
	protected Long id;
	
	/**
	 * 充值订单号 - S_ORDER_NO
	 */
	private String orderNo;

	/**
	 * 支付平台ID - N_PAY_PLATFORM_ID
	 */
	private Long platformId;

	/**
	 * 对方订单号 - S_OTHER_ORDER_NO
	 */
	private String payPlatformOrderId;

	/**
	 * 卡类型ID - N_CARDTYPE_ID
	 */
	private Long cardTypeId;

	/**
	 * 充入帐号ID - N_AID
	 */
	private Long aid;

	/**
	 * 充值卡数量 - N_AMOUNT
	 */
	private Integer amount;

	/**
	 * 充值币种 - S_CURRENCY
	 */
	private String currency;
	
	/**
	 * 余额币种 - S_YUE_CURRENCY
	 */
	private String yueCurrency;

	/**
	 * 充入游戏ID  - N_GAME_ID
	 */
	private Long gameId;

	/**
	 * 充入游戏分区ID - N_GAREA_ID
	 */
	private Long gameAreaId;

	/**
	 * 充值活动ID - N_IMPREST_PLOY_ID
	 */
	private Long ployId;

	/**
	 * 活动赠品充入游戏分区ID N_GIFT_GAREA_ID
	 */
	private Long presentGameAreaId;
	
	/**
	 * 充值模式 - 为分辨充值来源,和{@link ImprestLog#imprestMode}的充值模式对应
	 */
	private String imprestMode;

	/**
	 * 订单生成时间 D_CREATE
	 */
	private Date createDate;

	/**
	 * 客户端IP N_IP
	 */
	private Long clientIp;

	/**
	 * 支付平台服务端IP  N_PAY_IP
	 */
	private Long payPlatformIp;

	/**
	 * 支付状态 S_PAY_STATE
	 */
	private PaymentState paymentState;

	/**
	 * 支付完成时间 D_PAY_END
	 */
	private Date completeDate;

	/**
	 * 充值状态 S_STATE
	 */
	private ImprestState imprestState;

	/**
	 * 订单金额
	 */
	private Float money;
	
	/**
	 * 余额支付金额
	 */
	private Float yueMoney;
	
	/**
	 * 订单金额币种 - 参照币种定义表 - S_MONEY_CURRENCY
	 */
	private String moneyCurrency;
	
	/**
	 * 第三方前台跳转地址 S_PAYPARTNER_FRONT_CALL
	 */
	private String partnerFrontUrl;
	
	/**
	 * 第三方异步通知地址 S_PAYPARTNER_FRONT_CALL
	 */
	private String partnerBackendUrl;
	
	/**
	 * 第三方订单号 S_PAYPARTNER_OTHER_ORDER_NO
	 */
	private String partnerOrderNo;
	
	/**
	 * 服务器 N_GSERVER_ID
	 * 
	 * 游戏支付接口使用，便于客服查看
	 */
	private Long serverId;
	
	/**
	 * 扩展字段  S_INFO
	 */
	private String extend;
	
	/**
	 * 面值数量  N_VALUE_AMOUNT，活动翻倍赠送使用
	 */
	private Long valueAmount;
	
	/**
	 * 收银台使用，支付申请号
	 */
	private Long merchantId;
	
	private String yuePayState;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Long getPlatformId() {
		return platformId;
	}

	public void setPlatformId(Long platformId) {
		this.platformId = platformId;
	}

	public String getPayPlatformOrderId() {
		return payPlatformOrderId;
	}

	public void setPayPlatformOrderId(String payPlatformOrderId) {
		this.payPlatformOrderId = payPlatformOrderId;
	}

	public Long getCardTypeId() {
		return cardTypeId;
	}

	public void setCardTypeId(Long cardTypeId) {
		this.cardTypeId = cardTypeId;
	}

	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getYueCurrency() {
		return yueCurrency;
	}

	public void setYueCurrency(String yueCurrency) {
		this.yueCurrency = yueCurrency;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public Long getGameAreaId() {
		return gameAreaId;
	}

	public void setGameAreaId(Long gameAreaId) {
		this.gameAreaId = gameAreaId;
	}

	public Long getPloyId() {
		return ployId;
	}

	public void setPloyId(Long ployId) {
		this.ployId = ployId;
	}

	public Long getPresentGameAreaId() {
		return presentGameAreaId;
	}

	public void setPresentGameAreaId(Long presentGameAreaId) {
		this.presentGameAreaId = presentGameAreaId;
	}

	public String getImprestMode() {
		return imprestMode;
	}

	public void setImprestMode(String imprestMode) {
		this.imprestMode = imprestMode;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getClientIp() {
		return clientIp;
	}

	public void setClientIp(Long clientIp) {
		this.clientIp = clientIp;
	}

	public Long getPayPlatformIp() {
		return payPlatformIp;
	}

	public void setPayPlatformIp(Long payPlatformIp) {
		this.payPlatformIp = payPlatformIp;
	}

	public PaymentState getPaymentState() {
		return paymentState;
	}

	public void setPaymentState(PaymentState paymentState) {
		this.paymentState = paymentState;
	}

	public Date getCompleteDate() {
		return completeDate;
	}

	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	public ImprestState getImprestState() {
		return imprestState;
	}

	public void setImprestState(ImprestState imprestState) {
		this.imprestState = imprestState;
	}

	public Float getMoney() {
		return money;
	}

	public void setMoney(Float money) {
		this.money = money;
	}

	public Float getYueMoney() {
		return yueMoney;
	}

	public void setYueMoney(Float yueMoney) {
		this.yueMoney = yueMoney;
	}

	public String getMoneyCurrency() {
		return moneyCurrency;
	}

	public void setMoneyCurrency(String moneyCurrency) {
		this.moneyCurrency = moneyCurrency;
	}

	public String getPartnerFrontUrl() {
		return partnerFrontUrl;
	}

	public void setPartnerFrontUrl(String partnerFrontUrl) {
		this.partnerFrontUrl = partnerFrontUrl;
	}

	public String getPartnerBackendUrl() {
		return partnerBackendUrl;
	}

	public void setPartnerBackendUrl(String partnerBackendUrl) {
		this.partnerBackendUrl = partnerBackendUrl;
	}

	public String getPartnerOrderNo() {
		return partnerOrderNo;
	}

	public void setPartnerOrderNo(String partnerOrderNo) {
		this.partnerOrderNo = partnerOrderNo;
	}

	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public Long getValueAmount() {
		return valueAmount;
	}

	public void setValueAmount(Long valueAmount) {
		this.valueAmount = valueAmount;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public String getYuePayState() {
		return yuePayState;
	}

	public void setYuePayState(String yuePayState) {
		this.yuePayState = yuePayState;
	}
	

}

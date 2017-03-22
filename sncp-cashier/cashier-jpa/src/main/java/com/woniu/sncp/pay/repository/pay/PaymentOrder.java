package com.woniu.sncp.pay.repository.pay;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.woniu.sncp.pojo.SingleKeyPojo;

/**
 * 支付订单表 - SN_PAY.PAY_ORDER
 * @author 
 */
@Entity
@Table(name = "PAY_ORDER")
public class PaymentOrder implements SingleKeyPojo {

	/**
	 * 序列化值
	 */
	private static final long serialVersionUID = 5696993866385677593L;

	/**
	 * 0 - 未支付
	 */
	public static final String PAYMENT_STATE_CREATED = "0";

	/**
	 * 1 - 支付成功
	 */
	public static final String PAYMENT_STATE_PAYED = "1";

	/**
	 * 2 - 支付失败
	 */
	public static final String PAYMENT_STATE_FAILED = "2";

	/**
	 * 0 - 未充值
	 */
	public static final String IMPREST_STATE_NOT_COMPLETED = "0";

	/**
	 * 1 - 已充值
	 */
	public static final String IMPREST_STATE_COMPLETED = "1";
	
	@Id
	@Column(name = "N_ORDER_ID")
	protected Long id;
	
	/**
	 * 充值订单号 - S_ORDER_NO
	 */
	@Column(name = "S_ORDER_NO", nullable = false, length = 40)
	private String orderNo;

	/**
	 * 支付平台ID - N_PAY_PLATFORM_ID
	 */
	@Column(name = "N_PAY_PLATFORM_ID", nullable = false)
	private Long platformId;

	/**
	 * 对方订单号 - S_OTHER_ORDER_NO
	 */
	@Column(name = "S_OTHER_ORDER_NO", nullable = false)
	private String payPlatformOrderId;

	/**
	 * 卡类型ID - N_CARDTYPE_ID
	 */
	@Column(name = "N_CARDTYPE_ID", nullable = false)
	private Long cardTypeId;

	/**
	 * 充入帐号ID - N_AID
	 */
	@Column(name = "N_AID", nullable = false)
	private Long aid;

	/**
	 * 充值卡数量 - N_AMOUNT
	 */
	@Column(name = "N_AMOUNT", nullable = false)
	private Integer amount;

	/**
	 * 充值币种 - S_CURRENCY
	 */
	@Column(name = "S_CURRENCY", nullable = false)
	private String currency;
	
	/**
	 * 余额币种 - S_YUE_CURRENCY
	 */
	@Column(name = "S_YUE_CURRENCY", nullable = true)
	private String yueCurrency;

	/**
	 * 充入游戏ID  - N_GAME_ID
	 */
	@Column(name = "N_GAME_ID", nullable = false)
	private Long gameId;

	/**
	 * 充入游戏分区ID - N_GAREA_ID
	 */
	@Column(name = "N_GAREA_ID", nullable = false)
	private Long gameAreaId;

	/**
	 * 充值活动ID - N_IMPREST_PLOY_ID
	 */
	@Column(name = "N_IMPREST_PLOY_ID", nullable = true)
	private Long ployId;

	/**
	 * 活动赠品充入游戏分区ID N_GIFT_GAREA_ID
	 */
	@Column(name = "N_GIFT_GAREA_ID", nullable = true)
	private Long presentGameAreaId;
	
	/**
	 * 充值模式 - 为分辨充值来源,和{@link ImprestLog#imprestMode}的充值模式对应
	 */
	@Column(name = "S_IMPREST_MODE", nullable = true)
	private String imprestMode;

	/**
	 * 订单生成时间 D_CREATE
	 */
	@Column(name = "D_CREATE", nullable = false)
	private Date createDate;

	/**
	 * 客户端IP N_IP
	 */
	@Column(name = "N_IP", nullable = true)
	private Long clientIp;

	/**
	 * 支付平台服务端IP  N_PAY_IP
	 */
	@Column(name = "N_PAY_IP", nullable = true)
	private Long payPlatformIp;

	/**
	 * 支付状态 S_PAY_STATE
	 */
	@Column(name = "S_PAY_STATE", nullable = false)
	private String paymentState;

	/**
	 * 支付完成时间 D_PAY_END
	 */
	@Column(name = "D_PAY_END", nullable = true)
	private Date completeDate;

	/**
	 * 充值状态 S_STATE
	 */
	@Column(name = "S_STATE", nullable = false)
	private String imprestState;

	/**
	 * 订单金额
	 */
	@Column(name = "N_MONEY", nullable = true)
	private Float money;
	
	/**
	 * 余额支付金额
	 */
	@Column(name = "N_YUE_MONEY",nullable = true)
	private Float yueMoney;
	
	/**
	 * 订单金额币种 - 参照币种定义表 - S_MONEY_CURRENCY
	 */
	@Column(name = "S_MONEY_CURRENCY")
	private String moneyCurrency;
	
	/**
	 * 第三方前台跳转地址 S_PAYPARTNER_FRONT_CALL
	 */
	@Column(name = "S_PAYPARTNER_FRONT_CALL", nullable = true)
	private String partnerFrontUrl;
	
	/**
	 * 第三方异步通知地址 S_PAYPARTNER_FRONT_CALL
	 */
	@Column(name = "S_PAYPARTNER_BACKEND_CALL", nullable = true)
	private String partnerBackendUrl;
	
	/**
	 * 第三方订单号 S_PAYPARTNER_OTHER_ORDER_NO
	 */
	@Column(name = "S_PAYPARTNER_OTHER_ORDER_NO", nullable = true)
	private String partnerOrderNo;
	
	/**
	 * 服务器 N_GSERVER_ID
	 * 
	 * 游戏支付接口使用，便于客服查看
	 */
	@Column(name = "N_GSERVER_ID", nullable = true)
	private Long serverId;
	
	/**
	 * 扩展字段  S_INFO
	 */
	@Column(name = "S_INFO", nullable = true)
	private String extend;
	
	/**
	 * 面值数量  N_VALUE_AMOUNT，活动翻倍赠送使用
	 */
	@Column(name = "N_VALUE_AMOUNT", nullable = true)
	private Long valueAmount;
	
	/**
	 * 收银台使用，支付申请号
	 */
	@Column(name = "N_MERCHANT_ID", nullable = true)
	private Long merchantId;
	
	@Column(name = "S_YUE_PAY_STATE", nullable = true)
	private String yuePayState;
	
	/**
	 * 收银台渠道商户号
	 */
	@Column(name = "S_MERCHANT_NO", nullable = true)
	private String merchantNo;

	@Transient
	private String imprestCardName;

	@Transient
	private String userName;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getCardTypeId() {
		return cardTypeId;
	}

	public void setCardTypeId(Long cardTypeId) {
		this.cardTypeId = cardTypeId;
	}

	public Long getClientIp() {
		return clientIp;
	}

	public void setClientIp(Long clientIp) {
		this.clientIp = clientIp;
	}

	public Date getCompleteDate() {
		return completeDate;
	}

	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Long getGameAreaId() {
		return gameAreaId;
	}

	public void setGameAreaId(Long gameAreaId) {
		this.gameAreaId = gameAreaId;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public String getImprestState() {
		return imprestState;
	}

	public void setImprestState(String imprestState) {
		this.imprestState = imprestState;
	}

	public Float getMoney() {
		return money;
	}

	public void setMoney(Float money) {
		this.money = money;
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

	public Long getPayPlatformIp() {
		return payPlatformIp;
	}

	public void setPayPlatformIp(Long payPlatformIp) {
		this.payPlatformIp = payPlatformIp;
	}

	public String getPayPlatformOrderId() {
		return payPlatformOrderId;
	}

	public void setPayPlatformOrderId(String payPlatformOrderId) {
		this.payPlatformOrderId = payPlatformOrderId;
	}

	public String getPaymentState() {
		return paymentState;
	}

	public void setPaymentState(String paymentState) {
		this.paymentState = paymentState;
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

	public String getImprestCardName() {
		return imprestCardName;
	}

	public void setImprestCardName(String imprestCardName) {
		this.imprestCardName = imprestCardName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMoneyCurrency() {
		return moneyCurrency;
	}

	public void setMoneyCurrency(String moneyCurrency) {
		this.moneyCurrency = moneyCurrency;
	}

	public String getImprestMode() {
		return imprestMode;
	}

	public void setImprestMode(String imprestMode) {
		this.imprestMode = imprestMode;
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

	public Float getYueMoney() {
		return yueMoney;
	}

	public void setYueMoney(Float yueMoney) {
		this.yueMoney = yueMoney;
	}
	

	public String getYuePayState() {
		return yuePayState;
	}

	public void setYuePayState(String yuePayState) {
		this.yuePayState = yuePayState;
	}

	public String getYueCurrency() {
		return yueCurrency;
	}

	public void setYueCurrency(String yueCurrency) {
		this.yueCurrency = yueCurrency;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}
	
}
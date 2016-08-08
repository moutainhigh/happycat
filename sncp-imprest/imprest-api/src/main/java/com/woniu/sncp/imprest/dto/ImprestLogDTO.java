package com.woniu.sncp.imprest.dto;

import java.io.Serializable;
import java.util.Date;


/**
 * 充值日志
 * @author chenyx
 *
 */
public class ImprestLogDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	/**
	 * 充入帐号ID - N_AID
	 */
	private Long aid;

	/**
	 * 充值卡类型ID - N_CARDTYPE_ID
	 */
	private Long cardTypeId;

	/**
	 * 充值卡数量 - N_AMOUNT
	 */
	private Integer amount;

	/**
	 * 充值卡号/订单号 - S_CARD_NO
	 */
	private String cardNo;

	/**
	 * 充值方式 - S_IMPREST_MODE
	 */
	private String imprestMode;

	/**
	 * 支付平台ID (实/虚卡充值或系统赠送,默认为0 其他方式充值,都会有对应的支付平台ID) - N_PAY_PLATFORM_ID
	 */
	private Long platformId;

	/**
	 * 充入游戏ID (如果不是充往指定的游戏,则此处为0,如:第1虚拟币;否则为此游戏ID.)- N_GAME_ID
	 */
	private Long gameId;

	/**
	 * 充入游戏分区ID(如果充入中心，此处为0;如果充入分区,则存储的是分区ID) - N_GAREA_ID
	 */
	private Long gameAreaId;

	/**
	 * 充值币种 ( 不同的卡类型对应不同的币种,若是一卡通也可动态决定充值币种)- S_CURRENCY
	 */
	private String currency;

	/**
	 * 实际充入金额(对应于充入币种的金额,如果有赠送点数的活动,那么实际充入的金额就是卡本身的金额+赠送的点数) - N_MONEY
	 */
	private Integer point;

	/**
	 * 充值卡批次号(充值方式为实/虚卡充值时,记录此卡的总批次号) - N_BATCH_ID
	 */
	private Long batchId;

	/**
	 * 充值时间 - D_IMPREST
	 */
	private Date imprestDate;

	/**
	 * 客户端IP - N_IP
	 */
	private Long ip;

	/**
	 * 充值活动ID(充值时没有促销活动则为空;若有促销活动则存储活动ID.) - N_IMPREST_PLOY_ID
	 */
	private Long ployId;

	/**
	 * 活动赠品充入游戏分区ID - N_GIFT_GAREA_ID
	 */
	private Long presentGameAreaId;

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

	public Long getCardTypeId() {
		return cardTypeId;
	}

	public void setCardTypeId(Long cardTypeId) {
		this.cardTypeId = cardTypeId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getImprestMode() {
		return imprestMode;
	}

	public void setImprestMode(String imprestMode) {
		this.imprestMode = imprestMode;
	}

	public Long getPlatformId() {
		return platformId;
	}

	public void setPlatformId(Long platformId) {
		this.platformId = platformId;
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getPoint() {
		return point;
	}

	public void setPoint(Integer point) {
		this.point = point;
	}

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public Date getImprestDate() {
		return imprestDate;
	}

	public void setImprestDate(Date imprestDate) {
		this.imprestDate = imprestDate;
	}

	public Long getIp() {
		return ip;
	}

	public void setIp(Long ip) {
		this.ip = ip;
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

	
}

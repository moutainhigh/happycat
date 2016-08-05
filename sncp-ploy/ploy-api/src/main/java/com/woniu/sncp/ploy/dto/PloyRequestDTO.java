package com.woniu.sncp.ploy.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 活动请求参数
 * @author chenyx
 *
 */
public class PloyRequestDTO implements Serializable {

	private static final long serialVersionUID = -2245186653016209105L;
	
	private Long gameId;
	
	private Long aid;
	
	private Long gameAreaId;
	
	private BigDecimal amount;
	
	private Long cardTypeId;
	
	private Long imprestLogId;
	
	private Boolean isEaiQuery;
	
	private String impOrderNo;
	
	private String valueAmount;
	
	private Date imprestDate;
	
	private Integer presentLevel;
	
	private Date eventTime;

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}

	public Long getGameAreaId() {
		return gameAreaId;
	}

	public void setGameAreaId(Long gameAreaId) {
		this.gameAreaId = gameAreaId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Long getCardTypeId() {
		return cardTypeId;
	}

	public void setCardTypeId(Long cardTypeId) {
		this.cardTypeId = cardTypeId;
	}

	public Long getImprestLogId() {
		return imprestLogId;
	}

	public void setImprestLogId(Long imprestLogId) {
		this.imprestLogId = imprestLogId;
	}

	public Boolean getIsEaiQuery() {
		return isEaiQuery;
	}

	public void setIsEaiQuery(Boolean isEaiQuery) {
		this.isEaiQuery = isEaiQuery;
	}

	public String getImpOrderNo() {
		return impOrderNo;
	}

	public void setImpOrderNo(String impOrderNo) {
		this.impOrderNo = impOrderNo;
	}

	public String getValueAmount() {
		return valueAmount;
	}

	public void setValueAmount(String valueAmount) {
		this.valueAmount = valueAmount;
	}

	public Date getImprestDate() {
		return imprestDate;
	}

	public void setImprestDate(Date imprestDate) {
		this.imprestDate = imprestDate;
	}

	public Integer getPresentLevel() {
		return presentLevel;
	}

	public void setPresentLevel(Integer presentLevel) {
		this.presentLevel = presentLevel;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
	
	
}

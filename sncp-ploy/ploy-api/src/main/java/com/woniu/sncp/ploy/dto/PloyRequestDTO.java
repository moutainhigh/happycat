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
	
	private String game;
	
	private String passport;
	
	private String gameArea;
	
	private BigDecimal amount;
	
	private String cardType;
	
	private String imprestLog;
	
	private Boolean isEaiQuery;
	
	private String impLogId;
	
	private String valueAmount;
	
	private Date imprestDate;
	
	private Integer presentLevel;
	
	private Date eventTime;

	public String getGame() {
		return game;
	}

	public void setGame(String game) {
		this.game = game;
	}

	public String getPassport() {
		return passport;
	}

	public void setPassport(String passport) {
		this.passport = passport;
	}

	public String getGameArea() {
		return gameArea;
	}

	public void setGameArea(String gameArea) {
		this.gameArea = gameArea;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getImprestLog() {
		return imprestLog;
	}

	public void setImprestLog(String imprestLog) {
		this.imprestLog = imprestLog;
	}

	public Boolean getIsEaiQuery() {
		return isEaiQuery;
	}

	public void setIsEaiQuery(Boolean isEaiQuery) {
		this.isEaiQuery = isEaiQuery;
	}

	public String getImpLogId() {
		return impLogId;
	}

	public void setImpLogId(String impLogId) {
		this.impLogId = impLogId;
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

	@Override
	public String toString() {
		return "PloyRequest [game=" + game + ", passport=" + passport + ", gameArea=" + gameArea + ", amount=" + amount
				+ ", cardType=" + cardType + ", imprestLog=" + imprestLog + ", isEaiQuery=" + isEaiQuery + ", impLogId="
				+ impLogId + ", valueAmount=" + valueAmount + ", imprestDate=" + imprestDate + ", presentLevel="
				+ presentLevel + ", eventTime=" + eventTime + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventTime == null) ? 0 : eventTime.hashCode());
		result = prime * result + ((game == null) ? 0 : game.hashCode());
		result = prime * result + ((passport == null) ? 0 : passport.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PloyRequestDTO other = (PloyRequestDTO) obj;
		if (eventTime == null) {
			if (other.eventTime != null)
				return false;
		} else if (!eventTime.equals(other.eventTime))
			return false;
		if (game == null) {
			if (other.game != null)
				return false;
		} else if (!game.equals(other.game))
			return false;
		if (passport == null) {
			if (other.passport != null)
				return false;
		} else if (!passport.equals(other.passport))
			return false;
		return true;
	}
	
}

package com.woniu.sncp.cbss.core.model.request;

public abstract class RequestParam implements Param {
	/**
	 * 
	 */
	private static final long serialVersionUID = 96498314455200285L;

	/*
	 * 帐号ID
	 */
	private Long aid;

	/*
	 * 通行证
	 */
	private String passport;

	/*
	 * 别名
	 */
	private String alias;

	/*
	 * 运营商ID
	 */
	private Long issuerId;

	/*
	 * 游戏ID
	 */
	private Long gameId;

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public Long getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(Long issuerId) {
		this.issuerId = issuerId;
	}

	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}

	public String getPassport() {
		return passport;
	}

	public void setPassport(String passport) {
		this.passport = passport;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}

package com.woniu.sncp.ploy.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动赠送明细
 * @author chenyx
 *
 */
public class LargessPropDTO implements Serializable{
	
	private static final long serialVersionUID = 4549766576659636566L;

	private Long aid;

	/**
	 * 赠送道具ID - N_LARGESS_ID
	 */
	private Long propsId;

	/**
	 * 数量 - N_LARGESS_AMOUNT
	 */
	private Integer amount;

	/**
	 * 游戏ID - N_GAME_ID
	 */
	private Long gameId;

	/**
	 * 充入游戏分区ID - N_GAREA_ID
	 */
	private Long gameAreaId;

	/**
	 * 生成时间 - D_CREATE
	 */
	private Date createDate;

	/**
	 * 赠送来源类型 - S_TYPE
	 */
	private String sourceType;

	/**
	 * 相关记录ID - N_RELATED_ID
	 */
	private Long relatedId;

	/**
	 * 状态 - S_STATE
	 */
	private String state;

	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}

	public Long getPropsId() {
		return propsId;
	}

	public void setPropsId(Long propsId) {
		this.propsId = propsId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public Long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(Long relatedId) {
		this.relatedId = relatedId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "LargessProps [aid=" + aid + ", propsId=" + propsId + ", amount=" + amount + ", gameId=" + gameId
				+ ", gameAreaId=" + gameAreaId + ", createDate=" + createDate + ", sourceType=" + sourceType
				+ ", relatedId=" + relatedId + ", state=" + state + "]";
	}
	
}

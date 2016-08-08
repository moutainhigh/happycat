package com.woniu.sncp.ploy.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "IMP_LARGESS_INFO", schema = "SN_IMPREST")
@SequenceGenerator(name = "SEQ_GEN", sequenceName = "SN_IMPREST.IMP_LARGESS_INFO_SQ")
public class LargessProps implements Serializable {

	private static final long serialVersionUID = 2477733118176733285L;
	
	public enum State {
		SENDING,SEND_SUCCESS,SEND_FAIL,NO_SEND
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_GEN")
	@Column(name = "N_ID")
	private Long id;

	/**
	 * 充入帐号ID - N_AID
	 */
	@Column(name = "N_AID")
	private Long aid;

	/**
	 * 赠送道具ID - N_LARGESS_ID
	 */
	@Column(name = "N_LARGESS_ID")
	private Long propsId;

	/**
	 * 数量 - N_LARGESS_AMOUNT
	 */
	@Column(name = "N_LARGESS_AMOUNT")
	private Integer amount;

	/**
	 * 游戏ID - N_GAME_ID
	 */
	@Column(name = "N_GAME_ID")
	private Long gameId;

	/**
	 * 充入游戏分区ID - N_GAREA_ID
	 */
	@Column(name = "N_GAREA_ID")
	private Long gameAreaId;

	/**
	 * 生成时间 - D_CREATE
	 */
	@Column(name = "D_CREATE")
	private Date createDate;

	/**
	 * 赠送来源类型 - S_TYPE
	 */
	@Column(name = "S_TYPE")
	private String sourceType;

	/**
	 * 相关记录ID - N_RELATED_ID
	 */
	@Column(name = "N_RELATED_ID")
	private Long relatedId;

	/**
	 * 状态 - S_STATE
	 */
	@Column(name = "S_STATE")
	private State state;

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

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
}

package com.woniu.sncp.ploy.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "PP_PRESENTS_PLOY_DETAIL", schema = "SN_PASSPORT")
@SequenceGenerator(name = "SEQ_GEN", sequenceName = "SN_PASSPORT.PP_PASSPORT_PLOY_DETAIL_SQ")
public class PassportPresentsPloyDetail implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID - N_ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_GEN")
	@Column(name = "N_ID")
	private Long id;


	/**
	 * 活动ID - N_PLOY_ID
	 */
	@Column(name = "N_PLOY_ID")
	private Long ployId;

	/**
	 * (赠品)道具ID - N_PROP_ID
	 */
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.DETACH)
	@JoinColumn(name = "N_PROP_ID", referencedColumnName = "N_PROP_ID", unique = true)
	private GameProps gameProp;

	/**
	 * 赠品数量 - N_AMOUNT
	 */
	@Column(name = "N_AMOUNT")
	private Integer amount;
	
	/**
	 * 赠品限制条件 - S_LIMIT
	 * (比如说 卡类型限制，卡批次限制，充值金额限制,天数等等)
	 */
	@Column(name = "S_LIMIT")
	private String limitCondition;
	
	/**
	 * 赠送描述 - S_NOTE
	 * (对赠品限制条件进行描述)
	 */
	@Column(name = "S_NOTE")
	private String note;
	
	/**
	 * 币种 
	 * 
	 */
	@Column(name = "S_CURRENCY")
	private String currency;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPloyId() {
		return ployId;
	}

	public void setPloyId(Long ployId) {
		this.ployId = ployId;
	}

	public GameProps getGameProp() {
		return gameProp;
	}

	public void setGameProp(GameProps gameProp) {
		this.gameProp = gameProp;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getLimitCondition() {
		return limitCondition;
	}

	public void setLimitCondition(String limitCondition) {
		this.limitCondition = limitCondition;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
}

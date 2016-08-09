package com.woniu.sncp.ploy.dto;

import java.io.Serializable;

/**
 * 活动赠送明细
 * @author chenyx
 *
 */
public class PresentsPloyDetailDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Long ployId;

	private GamePropsDTO gameProp;

	private Integer amount;
	
	private String limitCondition;
	
	private String note;
	
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

	public GamePropsDTO getGameProp() {
		return gameProp;
	}

	public void setGameProp(GamePropsDTO gameProp) {
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

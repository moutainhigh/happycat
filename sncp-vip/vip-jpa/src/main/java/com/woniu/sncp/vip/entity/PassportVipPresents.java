package com.woniu.sncp.vip.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PP_VIP_PRESENTS", schema = "SN_PASSPORT")
public class PassportVipPresents implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	private PassportVipPresentsPK id;
	
	@Column(name = "N_AMOUNT")
	private Integer amount;
	
	@Column(name = "N_MONTH")
	private Integer month;

	public PassportVipPresentsPK getId() {
		return id;
	}

	public void setId(PassportVipPresentsPK id) {
		this.id = id;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}
	
}

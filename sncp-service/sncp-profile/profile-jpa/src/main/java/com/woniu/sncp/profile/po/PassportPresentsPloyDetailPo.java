package com.woniu.sncp.profile.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * 帐号赠送活动明细表 - PP_PRESENTS_PLOY_DETAIL
 * 
 * @author wujian
 * @since 1.0
 */
@Entity
@Table(name = "PP_PRESENTS_PLOY_DETAIL" )
public class PassportPresentsPloyDetailPo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID - N_ID
	 */
	@Id
	@Column(name = "N_ID")
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * 活动ID - N_PLOY_ID
	 */
	@Column(name = "N_PLOY_ID")
	private Long ployId;

	/**
	 * (赠品)道具ID - N_PROP_ID
	 */
	@Column(name = "N_PROP_ID")
	private Long propsId;

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

	@Column(name = "S_CURRENCY")
	private String currency;
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Long getPloyId() {
		return ployId;
	}

	public void setPloyId(Long ployId) {
		this.ployId = ployId;
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
	
}

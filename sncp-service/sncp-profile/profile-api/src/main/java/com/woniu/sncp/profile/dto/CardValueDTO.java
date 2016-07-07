package com.woniu.sncp.profile.dto;

import java.io.Serializable;

/**
 * <p>descrption: 卡大类</p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class CardValueDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String dispName;
	private Double minValue;
	private String currency;
	private String type;
	private Long cardPoint;
	private Double cardPrice;
	private String currencyName;
	private String customValueFlag;
	private Long cardId;
	private Long paymentId;
	private Long dispOrder;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDispName() {
		return dispName;
	}
	public void setDispName(String dispName) {
		this.dispName = dispName;
	}
	public Double getMinValue() {
		return minValue;
	}
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getCardPoint() {
		return cardPoint;
	}
	public void setCardPoint(Long cardPoint) {
		this.cardPoint = cardPoint;
	}
	public Double getCardPrice() {
		return cardPrice;
	}
	public void setCardPrice(Double cardPrice) {
		this.cardPrice = cardPrice;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	public String getCustomValueFlag() {
		return customValueFlag;
	}
	public void setCustomValueFlag(String customValueFlag) {
		this.customValueFlag = customValueFlag;
	}
	public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}
	public Long getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}
	public Long getDispOrder() {
		return dispOrder;
	}
	public void setDispOrder(Long dispOrder) {
		this.dispOrder = dispOrder;
	}
	@Override
	public String toString() {
		return "CardValueDTO [id=" + id + ", dispName=" + dispName + ", minValue=" + minValue + ", currency=" + currency
				+ ", type=" + type + ", cardPoint=" + cardPoint + ", cardPrice=" + cardPrice + ", currencyName="
				+ currencyName + ", customValueFlag=" + customValueFlag + ", cardId=" + cardId + ", paymentId="
				+ paymentId + ", dispOrder=" + dispOrder + "]";
	}
	
}

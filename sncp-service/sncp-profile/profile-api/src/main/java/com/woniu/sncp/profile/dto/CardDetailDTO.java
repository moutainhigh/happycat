package com.woniu.sncp.profile.dto;

import java.io.Serializable;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年7月1日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class CardDetailDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private Long mainId;
	private String valueName;
	private String valueDesc;
	private Double price;
	private String dState;
	private Long paymentId;
	private Long dispOrder;
	private Long cardId;
	private Double cardPrice;
	private Long cardPoint;
	private String tState;
	private String currency;
	private String currencyName;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getMainId() {
		return mainId;
	}
	public void setMainId(Long mainId) {
		this.mainId = mainId;
	}
	public String getValueName() {
		return valueName;
	}
	public void setValueName(String valueName) {
		this.valueName = valueName;
	}
	public String getValueDesc() {
		return valueDesc;
	}
	public void setValueDesc(String valueDesc) {
		this.valueDesc = valueDesc;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getdState() {
		return dState;
	}
	public void setdState(String dState) {
		this.dState = dState;
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
	public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}
	public Double getCardPrice() {
		return cardPrice;
	}
	public void setCardPrice(Double cardPrice) {
		this.cardPrice = cardPrice;
	}
	public Long getCardPoint() {
		return cardPoint;
	}
	public void setCardPoint(Long cardPoint) {
		this.cardPoint = cardPoint;
	}
	public String gettState() {
		return tState;
	}
	public void settState(String tState) {
		this.tState = tState;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	@Override
	public String toString() {
		return "CardDetailDTO [id=" + id + ", mainId=" + mainId + ", valueName=" + valueName + ", valueDesc="
				+ valueDesc + ", price=" + price + ", dState=" + dState + ", paymentId=" + paymentId + ", dispOrder="
				+ dispOrder + ", cardId=" + cardId + ", cardPrice=" + cardPrice + ", cardPoint=" + cardPoint
				+ ", tState=" + tState + ", currency=" + currency + ", currencyName=" + currencyName + "]";
	}
}

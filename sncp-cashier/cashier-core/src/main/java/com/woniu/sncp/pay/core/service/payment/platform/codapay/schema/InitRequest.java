package com.woniu.sncp.pay.core.service.payment.platform.codapay.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InitRequest implements Serializable {
	private static final long serialVersionUID = 201203024L;

	private String apiKey = null;
	private String orderId = null;
	private short country = 0;
	private short currency = 0;
	private short payType = 0;
	private ArrayList<ItemInfo> items = null;
	private HashMap<String, String> profile	= null;
	
	public InitRequest() {
	}

	public InitRequest(String apiKey, String orderId, short country, 
			short currency, short payType, ArrayList<ItemInfo> items, 
			HashMap<String, String> profile) {
		this.setApiKey(apiKey);
		this.setOrderId(orderId);
		this.setCountry(country);
		this.setCurrency(currency);
		this.setPayType(payType);
		this.setItems(items);
		this.setProfile(profile);
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public short getCountry() {
		return country;
	}

	public void setCountry(short country) {
		this.country = country;
	}

	public short getCurrency() {
		return currency;
	}

	public void setCurrency(short currency) {
		this.currency = currency;
	}

	public short getPayType() {
		return payType;
	}

	public void setPayType(short payType) {
		this.payType = payType;
	}

	public ArrayList<ItemInfo> getItems() {
		return items;
	}

	public void setItems(ArrayList<ItemInfo> items) {
		this.items = items;
	}

	public HashMap<String, String> getProfile() {
		return profile;
	}

	public void setProfile(HashMap<String, String> profile) {
		this.profile = profile;
	}
	
	public String toString() {
		return "apiKey : " + apiKey + "\torderId : " + orderId + "\tcountry : " + country + "\tcurrency : " + currency + "\tpayType : " + payType;
	}
}

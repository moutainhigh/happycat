package com.woniu.sncp.pay.core.service.payment.platform.bluepay;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BluepayConfig {

	private Map<String, String> smsPayUrl = new HashMap<String, String>();
	private Map<String, String> cardPayUrl = new HashMap<String, String>();
	private Map<String, String> bankPayUrl = new HashMap<String, String>();

	private Map<String, String> payCheckUrl = new HashMap<String, String>();
	private JSONObject operator;

	public Map<String, String> getSmsPayUrl() {
		return smsPayUrl;
	}

	public void setSmsPayUrl(Map<String, String> smsPayUrl) {
		this.smsPayUrl = smsPayUrl;
	}

	public Map<String, String> getPayCheckUrl() {
		return payCheckUrl;
	}

	public void setPayCheckUrl(Map<String, String> payCheckUrl) {
		this.payCheckUrl = payCheckUrl;
	}

	public Map<String, String> getCardPayUrl() {
		return cardPayUrl;
	}

	public void setCardPayUrl(Map<String, String> cardPayUrl) {
		this.cardPayUrl = cardPayUrl;
	}

	public JSONObject getOperator() {
		return operator;
	}

	public void setOperator(JSONObject operator) {
		this.operator = operator;
	}

	public Map<String, String> getBankPayUrl() {
		return bankPayUrl;
	}

	public void setBankPayUrl(Map<String, String> bankPayUrl) {
		this.bankPayUrl = bankPayUrl;
	}
}

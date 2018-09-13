package com.woniu.sncp.pay.core.service.payment.platform.codapay.schema;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PaymentResult implements Serializable {
	private static final long serialVersionUID = 201203021L;

	private long txnId;
	private short resultCode = 0; // 0 - Success
	private String resultDesc;
	private String orderId = null;
	private double totalPrice = 0;
	private Map<String, String> profile;

	public PaymentResult() {
	}
	
	public PaymentResult(long txnId, short resultCode, String resultDesc, String orderId, double totalPrice, Map<String, String> profile) {
		this.txnId = txnId;
		this.resultCode = resultCode;
		this.resultDesc = resultDesc;
		this.orderId = orderId;
		this.totalPrice = totalPrice;
		this.profile = profile;
	}
	public long getTxnId() {
		return txnId;
	}
	public void setTxnId(long txnId) {
		this.txnId = txnId;
	}
	public short getResultCode() {
		return resultCode;
	}
	public void setResultCode(short resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultDesc() {
		return resultDesc;
	}
	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}
	public Map<String, String> getProfile() {
		return profile;
	}
	public void setProfile(Map<String, String> profile) {
		this.profile = profile;
	}
}

package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

public class Payload {
	private String requestID;
	private Error error;
	private Payment payment;
	
	public String getRequestID() {
		return requestID;
	}
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	public Error getError() {
		return error;
	}
	public void setError(Error error) {
		this.error = error;
	}
	public Payment getPayment() {
		return payment;
	}
	public void setPayment(Payment payment) {
		this.payment = payment;
	}
}
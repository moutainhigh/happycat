package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

public class GetTransactionPostback {
	
	private String version;
	private Params params;
	
	private Payload payload;
	
	public GetTransactionPostback() {
	}

	public GetTransactionPostback(String version, String trackingID) {
		super();
		this.version = version;
		this.params = new Params(trackingID);
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Params getParams() {
		return params;
	}
	public void setParams(Params params) {
		this.params = params;
	}
	public Payload getPayload() {
		return payload;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}
	
}
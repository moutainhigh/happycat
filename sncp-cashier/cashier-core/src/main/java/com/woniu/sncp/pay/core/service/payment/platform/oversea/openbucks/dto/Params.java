package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

public class Params {
	
	private String trackingID;
	
	public Params() {
	}

	public Params(String trackingID) {
		super();
		this.trackingID = trackingID;
	}

	public String getTrackingID() {
		return trackingID;
	}

	public void setTrackingID(String trackingID) {
		this.trackingID = trackingID;
	}
}
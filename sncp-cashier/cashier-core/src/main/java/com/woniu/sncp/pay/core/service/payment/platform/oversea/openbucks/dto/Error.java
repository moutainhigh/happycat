package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

public class Error {
	
	private String errorCode;
    private String errorDescription;
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
}
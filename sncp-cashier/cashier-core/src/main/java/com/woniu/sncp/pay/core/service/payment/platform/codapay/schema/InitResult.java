package com.woniu.sncp.pay.core.service.payment.platform.codapay.schema;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InitResult implements Serializable {
	private static final long serialVersionUID = 201203024L;

	private short resultCode = 0;
	private String resultDesc = null;
	private long txnId = 0;
	private Map<String, String> profile;
	
	public InitResult() {
	}

	public InitResult(long txnId, short resultCode, String resultDesc, Map<String, String> profile) {
		this.resultCode = resultCode;
		this.resultDesc = resultDesc;
		this.txnId = txnId;
		this.profile = profile;
	}

	public short getResultCode() {
		return resultCode;
	}
	public void setResultCode(short resultCode) {
		this.resultCode = resultCode;
	}
	public long getTxnId() {
		return txnId;
	}
	public void setTxnId(long txnId) {
		this.txnId = txnId;
	}
	public Map<String, String> getProfile() {
		return profile;
	}
	public void setProfile(Map<String, String> profile) {
		this.profile = profile;
	}
	public String getResultDesc() {
		return resultDesc;
	}
	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}
}

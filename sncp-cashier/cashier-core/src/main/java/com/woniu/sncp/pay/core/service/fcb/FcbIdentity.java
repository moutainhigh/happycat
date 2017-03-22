package com.woniu.sncp.pay.core.service.fcb;

import java.io.Serializable;

public class FcbIdentity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8513862420112458409L;
	
	private String account;
	private String phone;
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
}

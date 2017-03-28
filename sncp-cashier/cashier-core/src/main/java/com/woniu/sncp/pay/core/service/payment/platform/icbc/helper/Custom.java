package com.woniu.sncp.pay.core.service.payment.platform.icbc.helper;

import javax.xml.bind.annotation.XmlElement;


public class Custom {
	
	private int verifyJoinFlag = 0;
	private String language = "ZH_CN";
	
	private String joinFlag;
	private String userNum;

	public int getVerifyJoinFlag() {
		return verifyJoinFlag;
	}
	public void setVerifyJoinFlag(int verifyJoinFlag) {
		this.verifyJoinFlag = verifyJoinFlag;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	
	@XmlElement(name="JoinFlag")
	public String getJoinFlag() {
		return joinFlag;
	}
	public void setJoinFlag(String joinFlag) {
		this.joinFlag = joinFlag;
	}
	@XmlElement(name="UserNum")
	public String getUserNum() {
		return userNum;
	}
	public void setUserNum(String userNum) {
		this.userNum = userNum;
	}
	
}

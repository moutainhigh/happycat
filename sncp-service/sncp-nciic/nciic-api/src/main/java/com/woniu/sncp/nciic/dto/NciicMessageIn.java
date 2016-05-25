package com.woniu.sncp.nciic.dto;

import java.io.Serializable;

/**
 * 实名认证信息
 * 
 */
public class NciicMessageIn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3756098010967356541L;

	public NciicMessageIn(String userName, String identityNo) {
		this.userName = userName;
		this.identityNo = identityNo;
	}

	/**
	 * 姓名
	 */
	private String userName;

	/**
	 * 身份证
	 */
	private String identityNo;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIdentityNo() {
		return identityNo;
	}

	public void setIdentityNo(String identityNo) {
		this.identityNo = identityNo;
	}

}

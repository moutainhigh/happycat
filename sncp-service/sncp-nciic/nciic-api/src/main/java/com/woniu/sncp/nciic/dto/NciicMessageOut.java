package com.woniu.sncp.nciic.dto;

import java.io.Serializable;

/**
 * 实名认证信息
 * 
 */
public class NciicMessageOut implements Serializable {
	public final static String SUCC_SAME = "一致";
	/**
	 * 
	 */
	private static final long serialVersionUID = 3756098010967356541L;

	public NciicMessageOut(String userName, String identityNo) {
		this.userName = userName;
		this.identityNo = identityNo;
	}

	/**
	 * 验证结果如果有错误信息,此值不为空时,才会有验证结果
	 */
	private String errorInfo;

	/**
	 * 姓名验证结果
	 */
	private String userNameResult;
	/**
	 * 身份证验证结果
	 */
	private String identityNoResult;

	/**
	 * 姓名
	 */
	private String userName;

	/**
	 * 身份证
	 */
	private String identityNo;

	public boolean actualResult() {
		return (errorInfo == null || "".equals(errorInfo.trim())) && SUCC_SAME.equals(getUserNameResult()) && SUCC_SAME.equals(getIdentityNoResult());
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	public String getUserNameResult() {
		return userNameResult;
	}

	public void setUserNameResult(String userNameResult) {
		this.userNameResult = userNameResult;
	}

	public String getIdentityNoResult() {
		return identityNoResult;
	}

	public void setIdentityNoResult(String identityNoResult) {
		this.identityNoResult = identityNoResult;
	}

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

package com.woniu.sncp.fcm.dto;

import java.io.Serializable;

/**
 * 是否防沉迷请求接口参数
 * @author chenyx
 * @date 2016年5月6日
 */
public class FcmIsNeedReqDto implements Serializable {

	private static final long serialVersionUID = -6579046814421953511L;
	
	/**
	 * 真实姓名
	 */
	private String realName;
	
	/**
	 * 身份证号码
	 */
	private String identity;
	
	/**
	 * 是否通过防沉迷
	 */
	private String isPassed;

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getIsPassed() {
		return isPassed;
	}

	public void setIsPassed(String isPassed) {
		this.isPassed = isPassed;
	}
}

package com.woniu.sncp.vip.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PassportVipPresentsPK implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "S_VIP_LEVEL")
	private String vipLevel;
	
	@Column(name = "S_SEND_LEVEL")
	private String sendLevel;

	public String getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getSendLevel() {
		return sendLevel;
	}

	public void setSendLevel(String sendLevel) {
		this.sendLevel = sendLevel;
	}
}

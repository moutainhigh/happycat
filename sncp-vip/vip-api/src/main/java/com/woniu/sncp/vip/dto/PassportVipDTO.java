package com.woniu.sncp.vip.dto;

import java.io.Serializable;

public class PassportVipDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long aid;
	
	private String vipLevel;

	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}

	public String getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}
}

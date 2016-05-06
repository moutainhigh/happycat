package com.woniu.sncp.fcm.dto;

import java.io.Serializable;

/**
 * 防沉迷在线时长累计请求参数
 * @author chenyx
 * @date 2016年5月6日
 */
public class FcmTimeOnlineReqDto extends FcmTimeOnlineDto implements Serializable {

	private static final long serialVersionUID = -4843756735566229313L;
	
	/**
	 * 在线时长（秒）
	 */
	private Long onlineTime;

	public Long getOnlineTime() {
		return onlineTime;
	}

	public void setOnlineTime(Long onlineTime) {
		this.onlineTime = onlineTime;
	}

}

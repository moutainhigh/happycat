package com.woniu.sncp.fcm.dto;

import java.io.Serializable;

/**
 * 防沉迷在线返回
 * @author chenyx
 * @date 2016年5月6日
 */
public class FcmTimeOnlineRespDto extends FcmTimeOnlineDto implements Serializable {

	
	private static final long serialVersionUID = 2487279689619727990L;
	
	/**
	 * 累计在线时长（秒）
	 */
	private Long totalTime;

	public Long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Long totalTime) {
		this.totalTime = totalTime;
	}
	
}

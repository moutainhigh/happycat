package com.woniu.sncp.fcm.dto;

import java.io.Serializable;

/**
 * 防沉迷游戏配置
 * @author chenyx
 * @date 2016年5月4日
 */
public class FcmGameProfile implements Serializable {

	private static final long serialVersionUID = 7590811202479166457L;
	
	private String id;
	
	private String gameId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}

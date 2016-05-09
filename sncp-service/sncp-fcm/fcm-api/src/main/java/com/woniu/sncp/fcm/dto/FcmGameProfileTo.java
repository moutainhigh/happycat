package com.woniu.sncp.fcm.dto;

import java.io.Serializable;

/**
 * 防沉迷游戏配置
 * @author chenyx
 * @date 2016年5月4日
 */
public class FcmGameProfileTo implements Serializable {

	private static final long serialVersionUID = 7590811202479166457L;
	
	private String id;
	
	private Long aoId;
	
	private Long gameId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getAoId() {
		return aoId;
	}

	public void setAoId(Long aoId) {
		this.aoId = aoId;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

}

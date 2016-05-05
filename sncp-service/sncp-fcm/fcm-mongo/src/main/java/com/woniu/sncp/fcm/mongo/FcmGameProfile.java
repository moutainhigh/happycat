package com.woniu.sncp.fcm.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

/**
 * mongoDB对象
 * @author chenyx
 * @date 2016年5月4日
 */
public class FcmGameProfile implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
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

}

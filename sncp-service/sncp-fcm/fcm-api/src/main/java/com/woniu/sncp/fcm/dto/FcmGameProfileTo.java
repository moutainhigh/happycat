package com.woniu.sncp.fcm.dto;

import java.io.Serializable;

/**
 * 防沉迷游戏配置
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
public class FcmGameProfileTo implements Serializable {

	private static final long serialVersionUID = 7590811202479166457L;
	
	private String id;
	
	/**
	 * 运营商ID
	 */
	private Long aoId;
	
	/**
	 * 游戏ID
	 */
	private Long gameId;

	/**
	 * @return 主键
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id 主键
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return 运营商ID
	 */
	public Long getAoId() {
		return aoId;
	}

	/**
	 * @param aoId 运营商ID
	 */
	public void setAoId(Long aoId) {
		this.aoId = aoId;
	}

	/**
	 * @return 游戏ID
	 */
	public Long getGameId() {
		return gameId;
	}

	/**
	 * @param gameId 游戏ID
	 */
	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

}

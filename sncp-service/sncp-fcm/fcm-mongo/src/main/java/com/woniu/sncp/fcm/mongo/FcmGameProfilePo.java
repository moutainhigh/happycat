package com.woniu.sncp.fcm.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * mongoDB对象
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
@Document(collection = "pp_aoperator_games")
public class FcmGameProfilePo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	@Field("N_AOID")
	private Long aoId;
	@Field("N_GAME_ID")
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

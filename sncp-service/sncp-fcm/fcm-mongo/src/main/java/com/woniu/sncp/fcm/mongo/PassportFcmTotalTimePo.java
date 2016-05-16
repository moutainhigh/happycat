package com.woniu.sncp.fcm.mongo;

import java.io.Serializable;
import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "pp_fcm_totaltime")
public class PassportFcmTotalTimePo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7746452028165460285L;
	
	@Id
	private String id;
	@Field("S_IDENTITY")
	private String identity;
	@Field("N_GAME_ID")
	private Long gameId;
	@Field("N_TIME")
	private Long time;
	@Field("N_LEAVE_TIME")
	private Long leaveTime;
	@Field("D_LAST_CHANGE")
	private Date lastChange;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public Long getGameId() {
		return gameId;
	}
	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Long getLeaveTime() {
		return leaveTime;
	}
	public void setLeaveTime(Long leaveTime) {
		this.leaveTime = leaveTime;
	}
	public Date getLastChange() {
		return lastChange;
	}
	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}
	
	public String toString(){
		return "PassportFcmTotalTime  identity:"+identity+",gameId:"+gameId+",time:"+time+",leaveTime:"+leaveTime+",lastChange:"+lastChange;
	}
}

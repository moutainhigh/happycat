package com.woniu.sncp.fcm.dto;

import java.io.Serializable;
import java.util.Date;

public class PassportFcmTotalTimeTo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7746452028165460285L;

	private String identity;
	private Long gameId;
	private Long time;
	private Long leaveTime;
	private Date lastChange;

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

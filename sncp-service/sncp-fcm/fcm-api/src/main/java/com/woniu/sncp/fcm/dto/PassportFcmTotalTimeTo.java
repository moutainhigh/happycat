package com.woniu.sncp.fcm.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 防沉迷累计时间
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
public class PassportFcmTotalTimeTo implements Serializable{
	
	
	private static final long serialVersionUID = 7746452028165460285L;

	private String identity;
	private Long gameId;
	private Long time;
	private Long leaveTime;
	private Date lastChange;

	/**
	 * @return 身份证
	 */
	public String getIdentity() {
		return identity;
	}
	/**
	 * @param identity 身份证
	 */
	public void setIdentity(String identity) {
		this.identity = identity;
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
	
	/**
	 * @return 在线时长（秒）
	 */
	public Long getTime() {
		return time;
	}
	
	/**
	 * @param time 在线时长（秒）
	 */
	public void setTime(Long time) {
		this.time = time;
	}
	
	/**
	 * @return 离线时长（秒）
	 */
	public Long getLeaveTime() {
		return leaveTime;
	}
	
	/**
	 * @param leaveTime 离线时长（秒）
	 */
	public void setLeaveTime(Long leaveTime) {
		this.leaveTime = leaveTime;
	}
	
	/**
	 * @return 最后修改时间
	 */
	public Date getLastChange() {
		return lastChange;
	}
	
	/**
	 * @param lastChange 最后修改时间
	 */
	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}
	
	public String toString(){
		return "identity:"+identity+",gameId:"+gameId+",time:"+time+",leaveTime:"+leaveTime+",lastChange:"+lastChange;
	}
}

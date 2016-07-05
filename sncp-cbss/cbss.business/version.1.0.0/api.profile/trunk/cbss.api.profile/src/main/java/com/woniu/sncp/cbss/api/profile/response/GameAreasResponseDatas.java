package com.woniu.sncp.cbss.api.profile.response;

import java.io.Serializable;
import java.util.Set;

/**
 * 
 * <p>descrption: 游戏配置响应</p>
 * 
 * @author fuzl
 * @date   2016年7月5日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class GameAreasResponseDatas implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long groupId;
	private String groupName;
	private Long sequence;
	private Long gameId;
	private Set<GameAreaResponseDatas> areas;
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Long getSequence() {
		return sequence;
	}
	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}
	public Long getGameId() {
		return gameId;
	}
	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
	public Set<GameAreaResponseDatas> getAreas() {
		return areas;
	}
	public void setAreas(Set<GameAreaResponseDatas> areas) {
		this.areas = areas;
	}
	
}
